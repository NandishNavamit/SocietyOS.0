package com.database.societyproject.resident.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.resident.dto.ResidentRequest;
import com.database.societyproject.resident.repository.ResidentRepository;
import com.database.societyproject.user.service.UserService;
import org.junit.jupiter.api.Test;

class ResidentServiceTest {
    private final ResidentRepository repository = mock(ResidentRepository.class);
    private final UserService userService = mock(UserService.class);
    private final ResidentService service = new ResidentService(repository, userService);

    @Test
    void duplicateUserResidentProfileIsRejected() {
        when(repository.existsByUserId(1L)).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(new ResidentRequest(1L, true)));
        verifyNoInteractions(userService);
    }
}
