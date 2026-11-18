package com.database.societyproject.finance.service;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.common.exception.*;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.repository.MaintenanceBillRepository;
import com.database.societyproject.flat.repository.FlatRepository;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.security.service.FinanceOperationsAuthorizationService;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class MaintenanceBillService {
    private final MaintenanceBillRepository repository; private final ChargePlanService planService; private final FlatService flatService; private final FlatRepository flatRepository; private final FinanceOperationsAuthorizationService financeAccess;
    public MaintenanceBillService(MaintenanceBillRepository repository, ChargePlanService planService, FlatService flatService, FlatRepository flatRepository, FinanceOperationsAuthorizationService financeAccess) { this.repository = repository; this.planService = planService; this.flatService = flatService; this.flatRepository = flatRepository; this.financeAccess=financeAccess; }
    public MaintenanceBillResponse create(MaintenanceBillRequest request) { validatePeriod(request.billingPeriodStart(), request.billingPeriodEnd(), request.dueDate()); ChargePlan plan=planService.findEntity(request.chargePlanId()); financeAccess.requireOperator(plan.getSociety().getId()); ensureUnique(request.chargePlanId(), request.flatId(), request.billingPeriodStart()); MaintenanceBill bill = new MaintenanceBill(); apply(request, bill); return toResponse(repository.save(bill)); }
    @Transactional(readOnly = true) public List<MaintenanceBillResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public MaintenanceBillResponse findById(Long id) { MaintenanceBill bill=findEntity(id); financeAccess.requireOperator(bill.getFlat().getBlock().getSociety().getId()); return toResponse(bill); }
    @Transactional(readOnly = true) public List<MaintenanceBillResponse> findByFlatId(Long id) { Flat flat=flatService.findEntity(id); financeAccess.requireOperator(flat.getBlock().getSociety().getId()); return repository.findByFlatId(id).stream().map(this::toResponse).toList(); }
    /** Used only after ResidentWorkspaceService has verified that the caller occupies this flat. */
    @Transactional(readOnly = true) public List<MaintenanceBillResponse> findByFlatIdForResidentWorkspace(Long id) { return repository.findByFlatId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<MaintenanceBillResponse> findBySocietyId(Long id) { financeAccess.requireOperator(id); return repository.findByFlatBlockSocietyId(id).stream().map(this::toResponse).toList(); }
    public List<MaintenanceBillResponse> generate(Long planId, BillGenerationRequest request) {
        ChargePlan plan = planService.findEntity(planId); financeAccess.requireOperator(plan.getSociety().getId()); if (!plan.getActive()) throw new InvalidRequestException("Cannot generate bills from an inactive charge plan");
        LocalDate start = request.billingPeriodStart(); LocalDate end = periodEnd(plan.getFrequency(), start); LocalDate due = dueDate(plan, start, end);
        List<Flat> flats = flatRepository.findByBlockSocietyIdAndActiveTrue(plan.getSociety().getId());
        if (flats.stream().anyMatch(flat -> repository.existsByChargePlanIdAndFlatIdAndBillingPeriodStart(planId, flat.getId(), start))) {
            throw new DuplicateResourceException("Bills have already been generated for this plan and billing period");
        }
        return flats.stream().map(flat -> {
            MaintenanceBill bill = new MaintenanceBill(); bill.setChargePlan(plan); bill.setFlat(flat); bill.setBillingPeriodStart(start); bill.setBillingPeriodEnd(end); bill.setDueDate(due); bill.setAmount(plan.getAmount()); return toResponse(repository.save(bill));
        }).toList();
    }
    public MaintenanceBill findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Maintenance bill", id)); }
    void refreshStatus(MaintenanceBill bill, BigDecimal total) { bill.setPaidAmount(total); if (total.compareTo(bill.getAmount()) >= 0) bill.setStatus(BillStatus.PAID); else if (total.signum() > 0) bill.setStatus(BillStatus.PARTIALLY_PAID); else if (bill.getDueDate().isBefore(LocalDate.now())) bill.setStatus(BillStatus.OVERDUE); else bill.setStatus(BillStatus.PENDING); }
    private void apply(MaintenanceBillRequest r, MaintenanceBill b) { ChargePlan p = planService.findEntity(r.chargePlanId()); Flat f = flatService.findEntity(r.flatId()); if (!f.getBlock().getSociety().getId().equals(p.getSociety().getId())) throw new InvalidRequestException("Flat must belong to the charge plan's society"); b.setChargePlan(p); b.setFlat(f); b.setBillingPeriodStart(r.billingPeriodStart()); b.setBillingPeriodEnd(r.billingPeriodEnd()); b.setDueDate(r.dueDate()); b.setAmount(r.amount()); }
    private void ensureUnique(Long planId, Long flatId, LocalDate start) { if (repository.existsByChargePlanIdAndFlatIdAndBillingPeriodStart(planId, flatId, start)) throw new DuplicateResourceException("A bill already exists for this plan, flat, and billing period"); }
    private void validatePeriod(LocalDate start, LocalDate end, LocalDate due) { if (end.isBefore(start)) throw new InvalidRequestException("billingPeriodEnd cannot be before billingPeriodStart"); if (due.isBefore(start)) throw new InvalidRequestException("dueDate cannot be before billingPeriodStart"); }
    private LocalDate periodEnd(ChargeFrequency frequency, LocalDate start) { return switch (frequency) { case MONTHLY -> start.withDayOfMonth(start.lengthOfMonth()); case QUARTERLY -> start.plusMonths(3).minusDays(1); case ONE_TIME -> start; }; }
    private LocalDate dueDate(ChargePlan plan, LocalDate start, LocalDate end) { if (plan.getDueDayOfMonth() == null) return end; return start.withDayOfMonth(Math.min(plan.getDueDayOfMonth(), start.lengthOfMonth())); }
    private MaintenanceBillResponse toResponse(MaintenanceBill b) { return new MaintenanceBillResponse(b.getId(), b.getChargePlan().getId(), b.getChargePlan().getName(), b.getChargeDescription(), b.getFlat().getId(), b.getFlat().getFlatNumber(), b.getBillingPeriodStart(), b.getBillingPeriodEnd(), b.getDueDate(), b.getAmount(), b.getPaidAmount(), b.getStatus(), b.getCreatedAt()); }
}
