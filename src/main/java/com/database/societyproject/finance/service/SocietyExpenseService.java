package com.database.societyproject.finance.service;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.repository.SocietyExpenseRepository;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.security.service.FinanceOperationsAuthorizationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class SocietyExpenseService {
    private final SocietyExpenseRepository repository; private final SocietyService societyService; private final FinanceOperationsAuthorizationService financeAccess;
    public SocietyExpenseService(SocietyExpenseRepository repository, SocietyService societyService, FinanceOperationsAuthorizationService financeAccess) { this.repository = repository; this.societyService = societyService; this.financeAccess=financeAccess; }
    public SocietyExpenseResponse create(SocietyExpenseRequest r) { financeAccess.requireOperator(r.societyId()); SocietyExpense expense = new SocietyExpense(); apply(r, expense); return toResponse(repository.save(expense)); }
    @Transactional(readOnly = true) public List<SocietyExpenseResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public SocietyExpenseResponse findById(Long id) { SocietyExpense expense=findEntity(id); financeAccess.requireOperator(expense.getSociety().getId()); return toResponse(expense); }
    @Transactional(readOnly = true) public List<SocietyExpenseResponse> findBySocietyId(Long id) { financeAccess.requireOperator(id); societyService.findEntity(id); return repository.findBySocietyId(id).stream().map(this::toResponse).toList(); }
    public SocietyExpenseResponse update(Long id, SocietyExpenseRequest r) { SocietyExpense expense = findEntity(id); financeAccess.requireOperator(expense.getSociety().getId()); if(!expense.getSociety().getId().equals(r.societyId())) throw new IllegalArgumentException("Expenses cannot be moved to another society"); apply(r, expense); return toResponse(expense); }
    public void deactivate(Long id) { SocietyExpense expense=findEntity(id); financeAccess.requireOperator(expense.getSociety().getId()); expense.setActive(false); }
    private SocietyExpense findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Society expense", id)); }
    private void apply(SocietyExpenseRequest r, SocietyExpense e) { e.setSociety(societyService.findEntity(r.societyId())); e.setCategory(r.category()); e.setDescription(r.description()); e.setAmount(r.amount()); e.setExpenseDate(r.expenseDate()); e.setVendorName(r.vendorName()); e.setReferenceNumber(r.referenceNumber()); if (r.active() != null) e.setActive(r.active()); }
    private SocietyExpenseResponse toResponse(SocietyExpense e) { return new SocietyExpenseResponse(e.getId(), e.getSociety().getId(), e.getCategory(), e.getDescription(), e.getAmount(), e.getExpenseDate(), e.getVendorName(), e.getReferenceNumber(), e.getActive(), e.getCreatedAt()); }
}
