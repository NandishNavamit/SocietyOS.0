package com.database.societyproject.security.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.entity.*;
import com.database.societyproject.security.repository.*;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.repository.UserRepository;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final UserSocietyRoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SocietyAuthorizationService authorization;

    public AuthService(UserRepository userRepository, UserSocietyRoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, SocietyAuthorizationService authorization) {
        this.userRepository = userRepository; this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository; this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService; this.authorization = authorization;
    }

    public CurrentUserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) throw new DuplicateResourceException("Email is already registered");
        User user = new User();
        user.setFullName(request.fullName()); user.setEmail(request.email()); user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password())); user.setActive(true);
        User saved = userRepository.save(user);
        return currentUserResponse(saved, List.of());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new InvalidRequestException("Invalid email or password"));
        if (!Boolean.TRUE.equals(user.getActive()) || user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidRequestException("Invalid email or password");
        }
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        JwtService.TokenData token = jwtService.parse(request.refreshToken(), "refresh");
        RefreshToken refreshToken = refreshTokenRepository.findByTokenId(token.tokenId())
                .orElseThrow(() -> new InvalidRequestException("Refresh token is no longer valid"));
        if (Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())
                || !refreshToken.getUser().getId().equals(token.userId())) {
            throw new InvalidRequestException("Refresh token is no longer valid");
        }
        refreshToken.setRevoked(true);
        return issueTokens(refreshToken.getUser());
    }

    public void logout(RefreshRequest request) {
        JwtService.TokenData token = jwtService.parse(request.refreshToken(), "refresh");
        refreshTokenRepository.findByTokenId(token.tokenId()).ifPresent(refreshToken -> refreshToken.setRevoked(true));
    }

    public void changePassword(ChangePasswordRequest request) {
        Long userId = authorization.currentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser() {
        Long userId = authorization.currentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        List<UserRole> roles = roleRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(UserSocietyRole::getRole).filter(UserRole::isCurrentProductRole).distinct().toList();
        return currentUserResponse(user, roles);
    }

    private AuthResponse issueTokens(User user) {
        List<String> roles = roleRepository.findByUserIdAndActiveTrue(user.getId()).stream()
                .map(UserSocietyRole::getRole).filter(UserRole::isCurrentProductRole)
                .map(Enum::name).distinct().toList();
        String tokenId = UUID.randomUUID().toString();
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user); refresh.setTokenId(tokenId); refresh.setExpiresAt(jwtService.refreshTokenExpiry());
        refreshTokenRepository.save(refresh);
        return new AuthResponse(jwtService.createAccessToken(user.getId(), user.getEmail(), roles),
                jwtService.createRefreshToken(user.getId(), tokenId), "Bearer", jwtService.accessTokenSeconds());
    }

    private CurrentUserResponse currentUserResponse(User user, List<UserRole> roles) {
        return new CurrentUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(),
                user.getActive(), user.getCreatedAt(), roles);
    }
}
