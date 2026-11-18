package com.database.societyproject.flat.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.block.service.BlockService;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.flat.dto.FlatRequest;
import com.database.societyproject.flat.repository.FlatRepository;
import org.junit.jupiter.api.Test;

class FlatServiceTest {
    private final FlatRepository repository = mock(FlatRepository.class);
    private final BlockService blockService = mock(BlockService.class);
    private final FlatService service = new FlatService(repository, blockService);

    @Test
    void duplicateFlatNumberWithinBlockIsRejected() {
        FlatRequest request = new FlatRequest("101", 1, "2BHK", 900.0, true, 1L);
        when(repository.existsByBlockIdAndFlatNumber(1L, "101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(request));
        verifyNoInteractions(blockService);
    }
}
