package com.database.societyproject.maintenancebilling.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.finance.entity.BillStatus;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.maintenancebilling.dto.*;
import com.database.societyproject.maintenancebilling.entity.MonthlyMaintenanceBill;
import com.database.societyproject.maintenancebilling.repository.MonthlyMaintenanceBillRepository;
import com.database.societyproject.maintenancepolicy.entity.*;
import com.database.societyproject.maintenancepolicy.repository.MaintenancePolicyRequestRepository;
import com.database.societyproject.occupancy.entity.*;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MonthlyMaintenanceBillingService {
    private final MonthlyMaintenanceBillRepository bills; private final MaintenancePolicyRequestRepository policies;
    private final FlatRepository flats; private final FlatOccupancyRepository occupancies; private final SocietyService societies;
    private final SocietyAuthorizationService authorization;
    private final NotificationService notifications;
    public MonthlyMaintenanceBillingService(MonthlyMaintenanceBillRepository bills, MaintenancePolicyRequestRepository policies, FlatRepository flats, FlatOccupancyRepository occupancies, SocietyService societies, SocietyAuthorizationService authorization, NotificationService notifications) { this.bills = bills; this.policies = policies; this.flats = flats; this.occupancies = occupancies; this.societies = societies; this.authorization = authorization; this.notifications = notifications; }
    public MonthlyBillGenerationResponse generate(Long societyId, MonthlyBillGenerationRequest request) { authorization.requireManager(societyId); return generateInternal(societyId, month(request.billingMonth()), true); }
    public BillingMonthResponse currentBillingMonth() { return new BillingMonthResponse(YearMonth.now().atDay(1)); }
    public List<MonthlyMaintenanceBillResponse> bySociety(Long societyId) { authorization.requireManager(societyId); refreshOverdueBills(); return bills.findBySocietyIdOrderByBillingMonthDescFlatFlatNumberAsc(societyId).stream().map(this::response).toList(); }
    public List<MonthlyMaintenanceBillResponse> mine() {
        Long userId = authorization.currentUserId();
        List<Long> flatIds = occupancies.findByResidentUserId(userId).stream().filter(occupancy -> Boolean.TRUE.equals(occupancy.getActive()) && occupancy.getOccupancyType() == OccupancyType.OWNER && occupancy.getResidentRole() == ResidentRole.PRIMARY).map(occupancy -> occupancy.getFlat().getId()).distinct().toList();
        refreshOverdueBills(); return flatIds.isEmpty() ? List.of() : bills.findByFlatIdInOrderByBillingMonthDesc(flatIds).stream().map(this::response).toList();
    }
    @Transactional(readOnly = true) public List<OwnedFlatResponse> myFlats() {
        return occupancies.findByResidentUserId(authorization.currentUserId()).stream()
                .filter(occupancy -> Boolean.TRUE.equals(occupancy.getActive()) && occupancy.getOccupancyType() == OccupancyType.OWNER && occupancy.getResidentRole() == ResidentRole.PRIMARY)
                .map(occupancy -> new OwnedFlatResponse(occupancy.getFlat().getId(), occupancy.getFlat().getFlatNumber(), occupancy.getFlat().getBlock().getName(), occupancy.getFlat().getBlock().getSociety().getId(), occupancy.getFlat().getBlock().getSociety().getName()))
                .toList();
    }
    /** Generates the current month once at startup as well as daily at 00:10.
     * Generation is idempotent, so a restart can safely catch up a missed job. */
    @EventListener(ApplicationReadyEvent.class)
    public void generateCurrentMonthOnStartup() { generateCurrentMonthAndRefresh(); }
    @Scheduled(cron = "0 10 0 * * *") public void generateCurrentMonthAndRefresh() { LocalDate current = YearMonth.now().atDay(1); for (Society society : societies.findActiveEntities()) generateInternal(society.getId(), current, false); refreshOverdueBills(); }
    public void refreshOverdueBills() { LocalDate today = LocalDate.now(); for (MonthlyMaintenanceBill bill : bills.findByStatusNotOrderByDueDateAsc(BillStatus.PAID)) refresh(bill, today); }
    public void refreshBill(MonthlyMaintenanceBill bill) { refresh(bill, LocalDate.now()); }
    private MonthlyBillGenerationResponse generateInternal(Long societyId, LocalDate billingMonth, boolean requireApplicablePolicy) {
        Society society = societies.findEntity(societyId); Optional<MaintenancePolicyRequest> applicable = policies.findFirstBySocietyIdAndStatusAndEffectiveMonthLessThanEqualOrderByEffectiveMonthDesc(societyId, MaintenancePolicyStatus.APPROVED, billingMonth);
        if (applicable.isEmpty()) {
            if (requireApplicablePolicy) throw new InvalidRequestException("No approved maintenance policy is effective for " + billingMonth + ". Choose a month on or after the policy effective month.");
            return new MonthlyBillGenerationResponse(societyId, billingMonth, 0, 0);
        }
        MaintenancePolicyRequest policy = applicable.get(); int generated = 0, existing = 0;
        for (Flat flat : flats.findByBlockSocietyIdAndActiveTrue(societyId)) {
            if (bills.existsByFlatIdAndBillingMonth(flat.getId(), billingMonth)) { existing++; continue; }
            MonthlyMaintenanceBill bill = new MonthlyMaintenanceBill(); bill.setSociety(society); bill.setFlat(flat); bill.setPolicyRequest(policy); bill.setBillingMonth(billingMonth); bill.setDueDate(billingMonth.withDayOfMonth(Math.min(policy.getDueDayOfMonth(), billingMonth.lengthOfMonth()))); bill.setMaintenanceAmount(policy.getMonthlyAmount()); bill.setLateFeePerOverdueMonth(policy.getLateFeePerOverdueMonth()); bill.setLateFeeMonthsApplied(0); bill.setLateFeeAmount(BigDecimal.ZERO); bill.setTotalAmount(policy.getMonthlyAmount()); bills.save(bill); notifications.notifyMonthlyBillGenerated(bill); generated++;
        }
        return new MonthlyBillGenerationResponse(societyId, billingMonth, generated, existing);
    }
    private void refresh(MonthlyMaintenanceBill bill, LocalDate today) {
        if (bill.getPaidAmount().compareTo(bill.getTotalAmount()) >= 0) { bill.setStatus(BillStatus.PAID); return; }
        int monthsOverdue = today.isAfter(bill.getDueDate()) ? Math.max(0, (int) java.time.temporal.ChronoUnit.MONTHS.between(YearMonth.from(bill.getDueDate()), YearMonth.from(today))) : 0;
        if (monthsOverdue > bill.getLateFeeMonthsApplied()) { bill.setLateFeeMonthsApplied(monthsOverdue); BigDecimal lateFees = bill.getLateFeePerOverdueMonth().multiply(BigDecimal.valueOf(monthsOverdue)); bill.setLateFeeAmount(lateFees); bill.setTotalAmount(bill.getMaintenanceAmount().add(lateFees)); }
        bill.setStatus(today.isAfter(bill.getDueDate()) ? BillStatus.OVERDUE : (bill.getPaidAmount().signum() > 0 ? BillStatus.PARTIALLY_PAID : BillStatus.PENDING));
    }
    private LocalDate month(LocalDate value) { return value.withDayOfMonth(1); }
    private MonthlyMaintenanceBillResponse response(MonthlyMaintenanceBill bill) { BigDecimal outstanding = bill.getTotalAmount().subtract(bill.getPaidAmount()).max(BigDecimal.ZERO); return new MonthlyMaintenanceBillResponse(bill.getId(), bill.getSociety().getId(), bill.getFlat().getId(), bill.getFlat().getFlatNumber(), bill.getBillingMonth(), bill.getDueDate(), bill.getMaintenanceAmount(), bill.getLateFeePerOverdueMonth(), bill.getLateFeeMonthsApplied(), bill.getLateFeeAmount(), bill.getTotalAmount(), bill.getPaidAmount(), outstanding, bill.getStatus(), bill.getGeneratedAt()); }
}
