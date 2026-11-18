package com.database.societyproject.staff.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.database.societyproject.staff.entity.StaffAssignment;
import com.database.societyproject.staff.entity.StaffType;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.staff.dto.StaffAssignmentRequest;
import com.database.societyproject.staff.repository.StaffAssignmentRepository;
import com.database.societyproject.society.service.SocietyService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StaffAssignmentServiceTest {
    private final StaffAssignmentRepository repository = mock(StaffAssignmentRepository.class);
    private final StaffProfileService profileService = mock(StaffProfileService.class);
    private final SocietyService societyService = mock(SocietyService.class);
    private final StaffAssignmentService service = new StaffAssignmentService(repository, profileService, societyService, mock(com.database.societyproject.security.service.SecurityOperationsAuthorizationService.class));

    @Test
    void duplicateAssignmentForSameProfileAndSocietyIsRejected() {
        when(repository.existsByStaffProfileIdAndSocietyId(1L, 2L)).thenReturn(true);
        StaffAssignmentRequest request = new StaffAssignmentRequest(1L, 2L, StaffType.PLUMBER, null, true);
        assertThrows(DuplicateResourceException.class, () -> service.create(request));
        verifyNoInteractions(profileService, societyService);
    }

    @Test
    void deletionDeactivatesAssignment() {
        StaffAssignment assignment = new StaffAssignment();
        assignment.setActive(true);
        when(repository.findById(1L)).thenReturn(Optional.of(assignment));
        service.deactivate(1L);
        assertFalse(assignment.getActive());
        verify(repository, never()).delete(any());
    }
}
