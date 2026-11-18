package com.database.societyproject.finance.service;
import com.database.societyproject.finance.entity.*;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.finance.dto.*;
import com.database.societyproject.finance.repository.ChargePlanRepository;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.security.service.FinanceOperationsAuthorizationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class ChargePlanService {
    private final ChargePlanRepository repository; private final SocietyService societyService; private final FinanceOperationsAuthorizationService financeAccess;
    public ChargePlanService(ChargePlanRepository repository, SocietyService societyService, FinanceOperationsAuthorizationService financeAccess) { this.repository = repository; this.societyService = societyService; this.financeAccess = financeAccess; }
    public ChargePlanResponse create(ChargePlanRequest request) { financeAccess.requireOperator(request.societyId()); ChargePlan plan = new ChargePlan(); apply(request, plan); return toResponse(repository.save(plan)); }
    @Transactional(readOnly = true) public List<ChargePlanResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public ChargePlanResponse findById(Long id) { ChargePlan plan=findEntity(id); financeAccess.requireOperator(plan.getSociety().getId()); return toResponse(plan); }
    @Transactional(readOnly = true) public List<ChargePlanResponse> findBySocietyId(Long id) { financeAccess.requireOperator(id); societyService.findEntity(id); return repository.findBySocietyId(id).stream().map(this::toResponse).toList(); }
    public ChargePlanResponse update(Long id, ChargePlanRequest request) { ChargePlan plan = findEntity(id); financeAccess.requireOperator(plan.getSociety().getId()); if(!plan.getSociety().getId().equals(request.societyId())) throw new IllegalArgumentException("Charge plans cannot be moved to another society"); apply(request, plan); return toResponse(plan); }
    public void deactivate(Long id) { ChargePlan plan=findEntity(id); financeAccess.requireOperator(plan.getSociety().getId()); plan.setActive(false); }
    public ChargePlan findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Charge plan", id)); }
    private void apply(ChargePlanRequest r, ChargePlan p) { Society society = societyService.findEntity(r.societyId()); p.setSociety(society); p.setName(r.name()); p.setAmount(r.amount()); p.setFrequency(r.frequency()); p.setDueDayOfMonth(r.dueDayOfMonth()); if (r.active() != null) p.setActive(r.active()); }
    private ChargePlanResponse toResponse(ChargePlan p) { return new ChargePlanResponse(p.getId(), p.getSociety().getId(), p.getName(), p.getAmount(), p.getFrequency(), p.getDueDayOfMonth(), p.getActive(), p.getCreatedAt()); }
}
