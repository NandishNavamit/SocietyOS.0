package com.database.societyproject.block.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.block.dto.BlockRequest;
import com.database.societyproject.block.repository.BlockRepository;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.society.service.SocietyService;
import org.junit.jupiter.api.Test;

class BlockServiceTest {
    private final BlockRepository repository = mock(BlockRepository.class);
    private final SocietyService societyService = mock(SocietyService.class);
    private final BlockService service = new BlockService(repository, societyService);

    @Test
    void duplicateBlockNameWithinSocietyIsRejected() {
        BlockRequest request = new BlockRequest("A", 5, 20, true, 1L);
        when(repository.existsBySocietyIdAndName(1L, "A")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(request));
        verifyNoInteractions(societyService);
    }
}
