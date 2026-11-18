package com.database.societyproject.visitor.service;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.visitor.entity.*;
import com.database.societyproject.visitor.repository.VisitorVisitRepository;
import com.database.societyproject.notification.service.NotificationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
class VisitorVisitServiceTest {
    private final VisitorVisitRepository repository = mock(VisitorVisitRepository.class);
    private final VisitorVisitService service = new VisitorVisitService(repository, mock(VisitorService.class), mock(SocietyService.class), mock(FlatService.class), mock(ResidentService.class), mock(VisitorAccessPassService.class), mock(FlatOccupancyRepository.class), mock(NotificationService.class), mock(com.database.societyproject.security.service.SecurityOperationsAuthorizationService.class));
    @Test void checkedOutVisitCannotBeCheckedOutAgain() { VisitorVisit visit = new VisitorVisit(); visit.setStatus(VisitStatus.CHECKED_OUT); when(repository.findById(1L)).thenReturn(Optional.of(visit)); assertThrows(InvalidRequestException.class, () -> service.checkOut(1L)); }
}
