package com.database.societyproject.complaint.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.complaint.dto.ComplaintStatusUpdateRequest;
import com.database.societyproject.complaint.entity.*;
import com.database.societyproject.complaint.repository.*;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.staff.service.StaffAssignmentService;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ComplaintServiceTest {
    private final ComplaintRepository repository = mock(ComplaintRepository.class);
    private final ComplaintStatusHistoryRepository historyRepository = mock(ComplaintStatusHistoryRepository.class);
    private final ComplaintService service = new ComplaintService(repository, historyRepository, mock(SocietyService.class), mock(FlatService.class), mock(ResidentService.class), mock(StaffAssignmentService.class), mock(FlatOccupancyRepository.class), mock(NotificationService.class), mock(SocietyAuthorizationService.class));

    @Test void invalidStatusTransitionIsRejected() {
        Complaint complaint = new Complaint(); complaint.setStatus(ComplaintStatus.OPEN);
        when(repository.findById(1L)).thenReturn(Optional.of(complaint));
        assertThrows(InvalidRequestException.class, () -> service.updateStatus(1L, new ComplaintStatusUpdateRequest(ComplaintStatus.RESOLVED, null)));
        verifyNoInteractions(historyRepository);
    }
}
