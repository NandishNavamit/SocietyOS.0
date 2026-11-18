package com.database.societyproject.security.service;

import com.database.societyproject.common.exception.InvalidRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
                      @Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String createAccessToken(Long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString()).claim("email", email).claim("roles", roles)
                .claim("type", "access").issuedAt(Date.from(now)).expiration(Date.from(now.plus(Duration.ofMinutes(accessTokenMinutes))))
                .signWith(key).compact();
    }

    public String createRefreshToken(Long userId, String tokenId) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString()).id(tokenId).claim("type", "refresh")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plus(Duration.ofDays(refreshTokenDays))))
                .signWith(key).compact();
    }

    public TokenData parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (!expectedType.equals(claims.get("type", String.class))) throw new InvalidRequestException("Invalid token type");
            Long userId = Long.valueOf(claims.getSubject());
            List<String> roles = claims.get("roles", List.class) == null ? List.of() : claims.get("roles", List.class);
            return new TokenData(userId, claims.get("email", String.class), claims.getId(), roles, claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidRequestException("Invalid or expired token");
        }
    }

    public long accessTokenSeconds() { return Duration.ofMinutes(accessTokenMinutes).toSeconds(); }
    public LocalDateTime refreshTokenExpiry() { return LocalDateTime.now().plusDays(refreshTokenDays); }
    public record TokenData(Long userId, String email, String tokenId, List<String> roles, Instant expiresAt) { }
}
