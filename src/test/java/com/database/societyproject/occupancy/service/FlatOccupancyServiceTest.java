package com.database.societyproject.occupancy.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.occupancy.entity.OccupancyType;
import com.database.societyproject.occupancy.entity.ResidentRole;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.dto.FlatOccupancyRequest;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.service.ResidentService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FlatOccupancyServiceTest {
    private final FlatOccupancyRepository repository = mock(FlatOccupancyRepository.class);
    private final FlatService flatService = mock(FlatService.class);
    private final ResidentService residentService = mock(ResidentService.class);
    private final FlatOccupancyService service = new FlatOccupancyService(repository, flatService, residentService);

    @Test
    void duplicateActiveOccupancyIsRejected() {
        FlatOccupancyRequest request = new FlatOccupancyRequest(1L, 2L, OccupancyType.TENANT, ResidentRole.PRIMARY, LocalDate.now(), null, true);
        when(repository.existsByFlatIdAndResidentIdAndActiveTrue(1L, 2L)).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(request));
        verifyNoInteractions(flatService, residentService);
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        FlatOccupancyRequest request = new FlatOccupancyRequest(1L, 2L, OccupancyType.TENANT, ResidentRole.PRIMARY, LocalDate.now(), LocalDate.now().minusDays(1), true);
        assertThrows(InvalidRequestException.class, () -> service.create(request));
        verifyNoInteractions(repository, flatService, residentService);
    }
}
