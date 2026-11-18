package com.database.societyproject.security.service;

import static org.junit.jupiter.api.Assertions.*;

import com.database.societyproject.common.exception.InvalidRequestException;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService service = new JwtService("this-is-a-test-jwt-secret-with-at-least-32-characters", 15, 7);

    @Test
    void accessTokenContainsExpectedIdentityAndRoles() {
        String token = service.createAccessToken(42L, "admin@example.com", List.of("ADMIN"));
        JwtService.TokenData data = service.parse(token, "access");
        assertEquals(42L, data.userId());
        assertEquals("admin@example.com", data.email());
        assertEquals(List.of("ADMIN"), data.roles());
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        String token = service.createRefreshToken(42L, "refresh-id");
        assertThrows(InvalidRequestException.class, () -> service.parse(token, "access"));
    }

    @Test
    void modifiedTokenIsRejected() {
        String token = service.createAccessToken(42L, "admin@example.com", List.of("ADMIN"));
        assertThrows(InvalidRequestException.class, () -> service.parse(token + "invalid", "access"));
    }
}
