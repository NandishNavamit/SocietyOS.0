package com.database.societyproject.report.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.database.societyproject.block.repository.BlockRepository;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.complaint.repository.ComplaintRepository;
import com.database.societyproject.finance.repository.*;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.notification.repository.NotificationRepository;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.report.dto.SocietyDashboardResponse;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.staff.repository.StaffAssignmentRepository;
import com.database.societyproject.visitor.repository.VisitorVisitRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportServiceTest {
    private final SocietyService societyService = mock(SocietyService.class);
    private final BlockRepository blockRepository = mock(BlockRepository.class);
    private final FlatRepository flatRepository = mock(FlatRepository.class);
    private final FlatOccupancyRepository occupancyRepository = mock(FlatOccupancyRepository.class);
    private final StaffAssignmentRepository staffRepository = mock(StaffAssignmentRepository.class);
    private final MaintenanceBillRepository billRepository = mock(MaintenanceBillRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final SocietyExpenseRepository expenseRepository = mock(SocietyExpenseRepository.class);
    private final ComplaintRepository complaintRepository = mock(ComplaintRepository.class);
    private final VisitorVisitRepository visitRepository = mock(VisitorVisitRepository.class);
    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final ReportService service = new ReportService(societyService, blockRepository, flatRepository,
            occupancyRepository, staffRepository, billRepository, paymentRepository, expenseRepository,
            complaintRepository, visitRepository, notificationRepository);

    @Test
    void dashboardReturnsZeroMetricsForANewSociety() {
        Society society = mock(Society.class);
        when(society.getName()).thenReturn("Green Valley");
        when(societyService.findEntity(1L)).thenReturn(society);
        when(flatRepository.findByBlockSocietyIdAndActiveTrue(1L)).thenReturn(List.of());
        when(occupancyRepository.findByFlatBlockSocietyIdAndActiveTrue(1L)).thenReturn(List.of());
        when(billRepository.findByFlatBlockSocietyId(1L)).thenReturn(List.of());
        when(complaintRepository.findBySocietyId(1L)).thenReturn(List.of());
        when(visitRepository.findBySocietyId(1L)).thenReturn(List.of());
        when(staffRepository.findBySocietyId(1L)).thenReturn(List.of());
        when(expenseRepository.findBySocietyId(1L)).thenReturn(List.of());
        when(notificationRepository.findBySocietyIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(blockRepository.findBySocietyId(1L)).thenReturn(List.of());

        SocietyDashboardResponse report = service.dashboard(1L);

        assertEquals("Green Valley", report.societyName());
        assertEquals(0, report.activeFlats());
        assertEquals(BigDecimal.ZERO, report.totalBilled());
        assertEquals(BigDecimal.ZERO, report.outstandingAmount());
    }

    @Test
    void reversedDateRangeIsRejected() {
        assertThrows(InvalidRequestException.class,
                () -> service.visitors(1L, LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)));
        verifyNoInteractions(societyService);
    }
}
