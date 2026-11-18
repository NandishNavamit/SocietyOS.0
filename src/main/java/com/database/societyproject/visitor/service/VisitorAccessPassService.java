package com.database.societyproject.visitor.service;

import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.visitor.dto.*;
import com.database.societyproject.visitor.entity.*;
import com.database.societyproject.visitor.repository.VisitorAccessPassRepository;
import com.database.societyproject.security.service.SecurityOperationsAuthorizationService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitorAccessPassService {
    private final VisitorAccessPassRepository repository;
    private final VisitorService visitorService;
    private final SocietyService societyService;
    private final FlatService flatService;
    private final ResidentService residentService;
    private final FlatOccupancyRepository occupancyRepository;
    private final SecurityOperationsAuthorizationService securityAccess;
    public VisitorAccessPassService(VisitorAccessPassRepository repository, VisitorService visitorService, SocietyService societyService, FlatService flatService, ResidentService residentService, FlatOccupancyRepository occupancyRepository, SecurityOperationsAuthorizationService securityAccess) {
        this.repository = repository; this.visitorService = visitorService; this.societyService = societyService; this.flatService = flatService; this.residentService = residentService; this.occupancyRepository = occupancyRepository; this.securityAccess = securityAccess;
    }
    public VisitorAccessPassResponse create(VisitorAccessPassRequest request) { securityAccess.requireSupervisor(request.societyId()); VisitorAccessPass pass = new VisitorAccessPass(); apply(request, pass); return toResponse(repository.save(pass)); }
    @Transactional(readOnly = true) public List<VisitorAccessPassResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public VisitorAccessPassResponse findById(Long id) { VisitorAccessPass pass=findEntity(id); securityAccess.requireOperator(pass.getSociety().getId()); return toResponse(pass); }
    @Transactional(readOnly = true) public List<VisitorAccessPassResponse> byVisitor(Long id) { visitorService.findEntity(id); return repository.findByVisitorId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<VisitorAccessPassResponse> bySociety(Long id) { securityAccess.requireOperator(id); societyService.findEntity(id); return repository.findBySocietyId(id).stream().map(this::toResponse).toList(); }
    public VisitorAccessPassResponse update(Long id, VisitorAccessPassRequest request) { VisitorAccessPass pass = findEntity(id); securityAccess.requireSupervisor(pass.getSociety().getId()); if (!pass.getSociety().getId().equals(request.societyId())) throw new InvalidRequestException("An access pass cannot be moved to another society"); if (pass.getStatus() != AccessPassStatus.PENDING) throw new InvalidRequestException("Only pending access passes can be updated"); apply(request, pass); return toResponse(pass); }
    public VisitorAccessPassResponse approve(Long id) { VisitorAccessPass pass = findEntity(id); securityAccess.requireSupervisor(pass.getSociety().getId()); if (pass.getStatus() != AccessPassStatus.PENDING) throw new InvalidRequestException("Only pending access passes can be approved"); pass.setStatus(AccessPassStatus.APPROVED); return toResponse(pass); }
    public VisitorAccessPassResponse revoke(Long id) { VisitorAccessPass pass = findEntity(id); securityAccess.requireSupervisor(pass.getSociety().getId()); if (pass.getStatus() != AccessPassStatus.REVOKED) pass.setStatus(AccessPassStatus.REVOKED); return toResponse(pass); }
    public VisitorAccessPass findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Visitor access pass", id)); }
    boolean isCurrentlyValid(VisitorAccessPass pass) { LocalDate today = LocalDate.now(); return pass.getStatus() == AccessPassStatus.APPROVED && !today.isBefore(pass.getValidFrom()) && (pass.getValidUntil() == null || !today.isAfter(pass.getValidUntil())); }
    private void apply(VisitorAccessPassRequest request, VisitorAccessPass pass) {
        validateDates(request); Society society = societyService.findEntity(request.societyId()); Flat flat = validateFlat(request.flatId(), society.getId()); Resident resident = validateResident(request.hostResidentId(), society.getId(), flat);
        pass.setVisitor(visitorService.findEntity(request.visitorId())); pass.setSociety(society); pass.setFlat(flat); pass.setHostResident(resident); pass.setAccessType(request.accessType()); pass.setValidFrom(request.validFrom()); pass.setValidUntil(request.validUntil()); pass.setPurpose(request.purpose());
    }
    private void validateDates(VisitorAccessPassRequest request) {
        if (request.accessType() == AccessType.PERMANENT && request.validUntil() != null) throw new InvalidRequestException("Permanent access passes must not have a validUntil date");
        if (request.accessType() != AccessType.PERMANENT && request.validUntil() == null) throw new InvalidRequestException("Temporary and one-time access passes require a validUntil date");
        if (request.validUntil() != null && request.validUntil().isBefore(request.validFrom())) throw new InvalidRequestException("validUntil cannot be before validFrom");
    }
    private Flat validateFlat(Long flatId, Long societyId) { if (flatId == null) return null; Flat flat = flatService.findEntity(flatId); if (!flat.getBlock().getSociety().getId().equals(societyId)) throw new InvalidRequestException("Flat must belong to the selected society"); return flat; }
    private Resident validateResident(Long residentId, Long societyId, Flat flat) { if (residentId == null) return null; Resident resident = residentService.findEntity(residentId); boolean member = flat == null ? occupancyRepository.existsByResidentIdAndFlatBlockSocietyIdAndActiveTrue(residentId, societyId) : occupancyRepository.existsByResidentIdAndFlatIdAndActiveTrue(residentId, flat.getId()); if (!member) throw new InvalidRequestException("Host resident must actively occupy the selected society or flat"); return resident; }
    private VisitorAccessPassResponse toResponse(VisitorAccessPass pass) { return new VisitorAccessPassResponse(pass.getId(), pass.getVisitor().getId(), pass.getSociety().getId(), pass.getFlat() == null ? null : pass.getFlat().getId(), pass.getHostResident() == null ? null : pass.getHostResident().getId(), pass.getAccessType(), pass.getStatus(), pass.getValidFrom(), pass.getValidUntil(), pass.getPurpose(), pass.getCreatedAt()); }
}
