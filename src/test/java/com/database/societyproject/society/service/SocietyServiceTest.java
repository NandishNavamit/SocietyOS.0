package com.database.societyproject.society.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.society.entity.Society;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.society.dto.SocietyRequest;
import com.database.societyproject.society.repository.SocietyRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SocietyServiceTest {
    private final SocietyRepository repository = mock(SocietyRepository.class);
    private final SocietyService service = new SocietyService(repository);

    @Test
    void deleteDeactivatesSocietyInsteadOfDeletingIt() {
        Society society = new Society();
        society.setActive(true);
        when(repository.findById(1L)).thenReturn(Optional.of(society));

        service.deactivate(1L);

        assertFalse(society.getActive());
        verify(repository, never()).delete(any());
    }

    @Test
    void missingSocietyProducesNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }
}
