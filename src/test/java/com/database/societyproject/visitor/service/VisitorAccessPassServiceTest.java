package com.database.societyproject.visitor.service;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.visitor.dto.VisitorAccessPassRequest;
import com.database.societyproject.visitor.entity.AccessType;
import com.database.societyproject.visitor.repository.VisitorAccessPassRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
class VisitorAccessPassServiceTest {
    private final VisitorAccessPassRepository repository = mock(VisitorAccessPassRepository.class);
    private final VisitorAccessPassService service = new VisitorAccessPassService(repository, mock(VisitorService.class), mock(SocietyService.class), mock(FlatService.class), mock(ResidentService.class), mock(FlatOccupancyRepository.class), mock(com.database.societyproject.security.service.SecurityOperationsAuthorizationService.class));
    @Test void temporaryPassRequiresEndDate() { VisitorAccessPassRequest request = new VisitorAccessPassRequest(1L, 1L, null, null, AccessType.TEMPORARY, LocalDate.now(), null, "Family visit"); assertThrows(InvalidRequestException.class, () -> service.create(request)); verifyNoInteractions(repository); }
}
