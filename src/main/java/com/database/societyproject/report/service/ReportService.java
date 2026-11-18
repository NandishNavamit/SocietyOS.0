package com.database.societyproject.report.service;

import com.database.societyproject.block.repository.BlockRepository;
import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.complaint.entity.*;
import com.database.societyproject.complaint.repository.ComplaintRepository;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.finance.repository.*;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.notification.repository.NotificationRepository;
import com.database.societyproject.occupancy.entity.FlatOccupancy;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.report.dto.*;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.staff.entity.*;
import com.database.societyproject.staff.repository.StaffAssignmentRepository;
import com.database.societyproject.visitor.entity.*;
import com.database.societyproject.visitor.repository.VisitorVisitRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final SocietyService societyService;
    private final BlockRepository blockRepository;
    private final FlatRepository flatRepository;
    private final FlatOccupancyRepository occupancyRepository;
    private final StaffAssignmentRepository staffRepository;
    private final MaintenanceBillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final SocietyExpenseRepository expenseRepository;
    private final ComplaintRepository complaintRepository;
    private final VisitorVisitRepository visitRepository;
    private final NotificationRepository notificationRepository;

    public ReportService(SocietyService societyService, BlockRepository blockRepository, FlatRepository flatRepository,
                         FlatOccupancyRepository occupancyRepository, StaffAssignmentRepository staffRepository,
                         MaintenanceBillRepository billRepository, PaymentRepository paymentRepository,
                         SocietyExpenseRepository expenseRepository, ComplaintRepository complaintRepository,
                         VisitorVisitRepository visitRepository, NotificationRepository notificationRepository) {
        this.societyService = societyService;
        this.blockRepository = blockRepository;
        this.flatRepository = flatRepository;
        this.occupancyRepository = occupancyRepository;
        this.staffRepository = staffRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.complaintRepository = complaintRepository;
        this.visitRepository = visitRepository;
        this.notificationRepository = notificationRepository;
    }

    public SocietyDashboardResponse dashboard(Long societyId) {
        Society society = societyService.findEntity(societyId);
        List<Flat> activeFlats = flatRepository.findByBlockSocietyIdAndActiveTrue(societyId);
        Set<Long> activeFlatIds = activeFlats.stream().map(Flat::getId).collect(Collectors.toSet());
        List<FlatOccupancy> activeOccupancies = occupancyRepository.findByFlatBlockSocietyIdAndActiveTrue(societyId).stream()
                .filter(occupancy -> activeFlatIds.contains(occupancy.getFlat().getId()))
                .toList();
        long occupiedFlats = activeOccupancies.stream().map(occupancy -> occupancy.getFlat().getId()).distinct().count();
        long activeResidents = activeOccupancies.stream()
                .map(FlatOccupancy::getResident)
                .filter(resident -> Boolean.TRUE.equals(resident.getActive())
                        && Boolean.TRUE.equals(resident.getUser().getActive()))
                .map(resident -> resident.getId()).distinct().count();
        List<MaintenanceBill> bills = billRepository.findByFlatBlockSocietyId(societyId);
        BigDecimal totalBilled = sumBills(bills, MaintenanceBill::getAmount);
        BigDecimal totalCollected = sumBills(bills, MaintenanceBill::getPaidAmount);
        BigDecimal outstanding = totalBilled.subtract(totalCollected);
        long overdue = overdueCount(bills);
        List<Complaint> complaints = complaintRepository.findBySocietyId(societyId);
        List<VisitorVisit> visits = visitRepository.findBySocietyId(societyId);
        long activeStaff = staffRepository.findBySocietyId(societyId).stream()
                .filter(assignment -> Boolean.TRUE.equals(assignment.getActive())).count();
        BigDecimal expenses = expenseRepository.findBySocietyId(societyId).stream()
                .filter(expense -> Boolean.TRUE.equals(expense.getActive()))
                .map(SocietyExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long unread = notificationRepository.findBySocietyIdOrderByCreatedAtDesc(societyId).stream()
                .filter(notification -> !Boolean.TRUE.equals(notification.getRead())).count();
        LocalDate today = LocalDate.now();
        return new SocietyDashboardResponse(societyId, society.getName(),
                blockRepository.findBySocietyId(societyId).stream().filter(block -> Boolean.TRUE.equals(block.getActive())).count(),
                activeFlats.size(), occupiedFlats, activeFlats.size() - occupiedFlats, activeResidents, activeStaff,
                totalBilled, totalCollected, outstanding, overdue, expenses,
                countComplaints(complaints, ComplaintStatus.OPEN), countComplaints(complaints, ComplaintStatus.ASSIGNED),
                countComplaints(complaints, ComplaintStatus.IN_PROGRESS), countComplaints(complaints, ComplaintStatus.RESOLVED),
                countComplaints(complaints, ComplaintStatus.CLOSED),
                visits.stream().filter(visit -> visit.getEntryTime() != null && today.equals(visit.getEntryTime().toLocalDate())).count(),
                visits.stream().filter(visit -> visit.getStatus() == VisitStatus.CHECKED_IN).count(), unread);
    }

    public MaintenanceReportResponse maintenance(Long societyId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        societyService.findEntity(societyId);
        List<MaintenanceBill> bills = billRepository.findByFlatBlockSocietyId(societyId).stream()
                .filter(bill -> within(bill.getBillingPeriodStart(), from, to)).toList();
        BigDecimal collected = paymentRepository.findByBillFlatBlockSocietyId(societyId).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED && within(payment.getPaymentDate(), from, to))
                .map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = expenseRepository.findBySocietyId(societyId).stream()
                .filter(expense -> Boolean.TRUE.equals(expense.getActive()) && within(expense.getExpenseDate(), from, to))
                .map(SocietyExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal billed = sumBills(bills, MaintenanceBill::getAmount);
        return new MaintenanceReportResponse(from, to, bills.size(), billed, collected,
                billed.subtract(sumBills(bills, MaintenanceBill::getPaidAmount)), overdueCount(bills), expenses);
    }

    public ComplaintReportResponse complaints(Long societyId) {
        societyService.findEntity(societyId);
        List<Complaint> complaints = complaintRepository.findBySocietyId(societyId);
        Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
        for (ComplaintStatus status : ComplaintStatus.values()) counts.put(status, countComplaints(complaints, status));
        return new ComplaintReportResponse(complaints.size(), counts);
    }

    public VisitorReportResponse visitors(Long societyId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        societyService.findEntity(societyId);
        List<VisitorVisit> visits = visitRepository.findBySocietyId(societyId).stream()
                .filter(visit -> visit.getEntryTime() != null && within(visit.getEntryTime().toLocalDate(), from, to)).toList();
        return new VisitorReportResponse(from, to, visits.size(), countVisits(visits, VisitStatus.CHECKED_IN),
                countVisits(visits, VisitStatus.CHECKED_OUT), countVisits(visits, VisitStatus.DENIED));
    }

    public StaffReportResponse staff(Long societyId) {
        societyService.findEntity(societyId);
        List<StaffAssignment> assignments = staffRepository.findBySocietyId(societyId).stream()
                .filter(assignment -> Boolean.TRUE.equals(assignment.getActive())).toList();
        Map<StaffType, Long> counts = new EnumMap<>(StaffType.class);
        for (StaffType type : StaffType.values()) {
            counts.put(type, assignments.stream().filter(assignment -> assignment.getStaffType() == type).count());
        }
        return new StaffReportResponse(assignments.size(), counts);
    }

    private BigDecimal sumBills(List<MaintenanceBill> bills, Function<MaintenanceBill, BigDecimal> value) {
        return bills.stream().map(value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long overdueCount(List<MaintenanceBill> bills) {
        LocalDate today = LocalDate.now();
        return bills.stream().filter(bill -> bill.getDueDate().isBefore(today)
                && bill.getPaidAmount().compareTo(bill.getAmount()) < 0).count();
    }

    private long countComplaints(List<Complaint> complaints, ComplaintStatus status) {
        return complaints.stream().filter(complaint -> complaint.getStatus() == status).count();
    }

    private long countVisits(List<VisitorVisit> visits, VisitStatus status) {
        return visits.stream().filter(visit -> visit.getStatus() == status).count();
    }

    private boolean within(LocalDate value, LocalDate from, LocalDate to) {
        return !value.isBefore(from) && !value.isAfter(to);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new InvalidRequestException("from and to dates are required, and to cannot be before from");
        }
    }
}
