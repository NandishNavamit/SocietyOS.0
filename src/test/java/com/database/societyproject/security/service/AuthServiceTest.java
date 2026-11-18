package com.database.societyproject.security.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.security.dto.LoginRequest;
import com.database.societyproject.security.repository.*;
import com.database.societyproject.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AuthService service = new AuthService(users, mock(UserSocietyRoleRepository.class),
            mock(RefreshTokenRepository.class), new BCryptPasswordEncoder(),
            new JwtService("this-is-a-test-jwt-secret-with-at-least-32-characters", 15, 7),
            mock(SocietyAuthorizationService.class));

    @Test
    void loginRejectsUnknownEmailWithoutLeakingWhichFieldWasWrong() {
        when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(InvalidRequestException.class, () -> service.login(new LoginRequest("missing@example.com", "incorrect")));
    }
}
