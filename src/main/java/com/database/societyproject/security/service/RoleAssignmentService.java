package com.database.societyproject.security.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.security.dto.*;
import com.database.societyproject.security.entity.*;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.service.UserService;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleAssignmentService {
    private final UserSocietyRoleRepository repository;
    private final UserService userService;
    private final SocietyService societyService;
    private final SocietyAuthorizationService authorization;

    public RoleAssignmentService(UserSocietyRoleRepository repository, UserService userService,
                                 SocietyService societyService, SocietyAuthorizationService authorization) {
        this.repository = repository; this.userService = userService; this.societyService = societyService; this.authorization = authorization;
    }

    public UserRoleAssignmentResponse assign(Long userId, UserRoleAssignmentRequest request) {
        if (!request.role().isCurrentProductRole()) {
            throw new InvalidRequestException("This retired role cannot be assigned");
        }
        validateAssignmentPermission(request);
        User user = userService.findEntity(userId);
        Society society = request.societyId() == null ? null : societyService.findEntity(request.societyId());
        if (request.role() == UserRole.ADMIN) {
            Optional<UserSocietyRole> existing = repository.findByUserIdAndSocietyIsNullAndRole(userId, UserRole.ADMIN);
            if (existing.isPresent()) {
                if (Boolean.TRUE.equals(existing.get().getActive())) throw new DuplicateResourceException("User already has the ADMIN role");
                existing.get().setActive(true);
                return toResponse(existing.get());
            }
        } else {
            if (requiresExistingMembership(request.role()) && !repository.existsByUserIdAndSocietyIdAndActiveTrue(userId, society.getId())) {
                throw new InvalidRequestException("A delegated role can only be assigned to an approved member of this society");
            }
            Optional<UserSocietyRole> existing = repository.findByUserIdAndSocietyIdAndRole(userId, society.getId(), request.role());
            if (existing.isPresent()) {
                if (Boolean.TRUE.equals(existing.get().getActive())) throw new DuplicateResourceException("User already has this role in the selected society");
                existing.get().setActive(true);
                return toResponse(existing.get());
            }
        }
        UserSocietyRole assignment = new UserSocietyRole();
        assignment.setUser(user); assignment.setSociety(society); assignment.setRole(request.role()); assignment.setActive(true);
        return toResponse(repository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<UserRoleAssignmentResponse> findByUser(Long userId) {
        if (!authorization.isAdmin() && !authorization.currentUserId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own roles");
        }
        userService.findEntity(userId);
        return repository.findByUserIdAndActiveTrue(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserRoleAssignmentResponse> findBySociety(Long societyId) {
        authorization.requireManager(societyId);
        return repository.findBySocietyIdAndActiveTrue(societyId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SocietyMemberResponse> findMembersBySociety(Long societyId) {
        authorization.requireManager(societyId);
        Map<Long, List<UserSocietyRole>> grouped = new LinkedHashMap<>();
        for (UserSocietyRole role : repository.findBySocietyIdAndActiveTrue(societyId)) {
            grouped.computeIfAbsent(role.getUser().getId(), ignored -> new ArrayList<>()).add(role);
        }
        return grouped.values().stream().map(assignments -> {
            User user = assignments.get(0).getUser();
            return new SocietyMemberResponse(user.getId(), user.getFullName(), user.getEmail(),
                    assignments.stream().map(UserSocietyRole::getRole).distinct().toList());
        }).toList();
    }

    public UserRoleAssignmentResponse deactivate(Long id) {
        UserSocietyRole assignment = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User society role", id));
        validateAssignmentPermission(new UserRoleAssignmentRequest(assignment.getSociety() == null ? null : assignment.getSociety().getId(), assignment.getRole()));
        assignment.setActive(false);
        return toResponse(assignment);
    }

    private void validateAssignmentPermission(UserRoleAssignmentRequest request) {
        if (request.role() == UserRole.ADMIN) {
            if (request.societyId() != null) throw new InvalidRequestException("ADMIN role must not be linked to a society");
            authorization.requireAdmin();
            return;
        }
        if (request.societyId() == null) throw new InvalidRequestException("A society is required for this role");
        if (authorization.isAdmin()) return;
        authorization.requireManager(request.societyId());
        if (request.role() == UserRole.SOCIETY_ADMIN) {
            throw new AccessDeniedException("Only a platform administrator can assign a society administrator role");
        }
    }

    private boolean requiresExistingMembership(UserRole role) { return false; }

    private UserRoleAssignmentResponse toResponse(UserSocietyRole role) {
        return new UserRoleAssignmentResponse(role.getId(), role.getUser().getId(),
                role.getSociety() == null ? null : role.getSociety().getId(), role.getRole(), role.getActive(), role.getCreatedAt());
    }
}
