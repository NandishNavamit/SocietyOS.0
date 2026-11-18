package com.database.societyproject.visitor.service;

import com.database.societyproject.common.exception.*;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.staff.entity.*;
import com.database.societyproject.staff.service.StaffAssignmentService;
import com.database.societyproject.visitor.dto.*;
import com.database.societyproject.visitor.entity.*;
import com.database.societyproject.visitor.repository.VisitorVisitRepository;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SecurityOperationsAuthorizationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitorVisitService {
    private final VisitorVisitRepository repository; private final VisitorService visitorService; private final SocietyService societyService; private final FlatService flatService; private final ResidentService residentService; private final VisitorAccessPassService passService; private final FlatOccupancyRepository occupancyRepository; private final NotificationService notificationService; private final SecurityOperationsAuthorizationService securityAccess;
    public VisitorVisitService(VisitorVisitRepository repository, VisitorService visitorService, SocietyService societyService, FlatService flatService, ResidentService residentService, VisitorAccessPassService passService, FlatOccupancyRepository occupancyRepository, NotificationService notificationService, SecurityOperationsAuthorizationService securityAccess) { this.repository = repository; this.visitorService = visitorService; this.societyService = societyService; this.flatService = flatService; this.residentService = residentService; this.passService = passService; this.occupancyRepository = occupancyRepository; this.notificationService = notificationService; this.securityAccess = securityAccess; }
    public VisitorVisitResponse checkIn(VisitorCheckInRequest request) {
        securityAccess.requireOperator(request.societyId()); Society society = societyService.findEntity(request.societyId()); Flat flat = validateFlat(request.flatId(), society.getId()); Resident host = validateHost(request.hostResidentId(), society.getId(), flat); StaffAssignment security = securityAccess.handlingAssignment(society.getId(), request.securityAssignmentId()); VisitorAccessPass pass = validatePass(request.accessPassId(), request.visitorId(), society.getId());
        VisitorVisit visit = new VisitorVisit(); visit.setVisitor(visitorService.findEntity(request.visitorId())); visit.setSociety(society); visit.setFlat(flat); visit.setHostResident(host); visit.setHandledBySecurity(security); visit.setAccessPass(pass); visit.setVisitorType(request.visitorType()); visit.setPurpose(request.purpose()); visit.setEntryTime(LocalDateTime.now()); VisitorVisit saved = repository.save(visit); notificationService.notifyVisitorCheckedIn(saved); return toResponse(saved);
    }
    @Transactional(readOnly = true) public List<VisitorVisitResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public VisitorVisitResponse findById(Long id) { VisitorVisit visit=findEntity(id); securityAccess.requireOperator(visit.getSociety().getId()); return toResponse(visit); }
    @Transactional(readOnly = true) public List<VisitorVisitResponse> bySociety(Long id) { securityAccess.requireOperator(id); societyService.findEntity(id); List<VisitorVisit> visits = securityAccess.isSupervisor(id) ? repository.findBySocietyId(id) : repository.findBySocietyIdAndEntryTimeAfterOrderByEntryTimeDesc(id, LocalDateTime.now().minusDays(1)); return visits.stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<VisitorVisitResponse> byFlat(Long id) { Flat flat=flatService.findEntity(id); securityAccess.requireOperator(flat.getBlock().getSociety().getId()); return repository.findByFlatId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<VisitorVisitResponse> byVisitor(Long id) { visitorService.findEntity(id); return repository.findByVisitorId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<VisitorVisitResponse> byStatus(VisitStatus status) { return repository.findByStatus(status).stream().map(this::toResponse).toList(); }
    public VisitorVisitResponse checkOut(Long id) { VisitorVisit visit = findEntity(id); securityAccess.requireOperator(visit.getSociety().getId()); if (visit.getStatus() != VisitStatus.CHECKED_IN) throw new InvalidRequestException("Only checked-in visits can be checked out"); visit.setStatus(VisitStatus.CHECKED_OUT); visit.setExitTime(LocalDateTime.now()); return toResponse(visit); }
    public VisitorVisitResponse deny(Long id) { VisitorVisit visit = findEntity(id); securityAccess.requireOperator(visit.getSociety().getId()); if (visit.getStatus() != VisitStatus.CHECKED_IN) throw new InvalidRequestException("Only checked-in visits can be denied"); visit.setStatus(VisitStatus.DENIED); visit.setExitTime(LocalDateTime.now()); return toResponse(visit); }
    private VisitorVisit findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Visitor visit", id)); }
    private Flat validateFlat(Long flatId, Long societyId) { if (flatId == null) return null; Flat flat = flatService.findEntity(flatId); if (!flat.getBlock().getSociety().getId().equals(societyId)) throw new InvalidRequestException("Flat must belong to the selected society"); return flat; }
    private Resident validateHost(Long residentId, Long societyId, Flat flat) { if (residentId == null) return null; Resident resident = residentService.findEntity(residentId); boolean member = flat == null ? occupancyRepository.existsByResidentIdAndFlatBlockSocietyIdAndActiveTrue(residentId, societyId) : occupancyRepository.existsByResidentIdAndFlatIdAndActiveTrue(residentId, flat.getId()); if (!member) throw new InvalidRequestException("Host resident must actively occupy the selected society or flat"); return resident; }
    private VisitorAccessPass validatePass(Long id, Long visitorId, Long societyId) { if (id == null) return null; VisitorAccessPass pass = passService.findEntity(id); if (!pass.getVisitor().getId().equals(visitorId) || !pass.getSociety().getId().equals(societyId) || !passService.isCurrentlyValid(pass)) throw new InvalidRequestException("Access pass must be approved, currently valid, and belong to this visitor and society"); return pass; }
    private VisitorVisitResponse toResponse(VisitorVisit visit) { return new VisitorVisitResponse(visit.getId(), visit.getVisitor().getId(), visit.getSociety().getId(), visit.getFlat() == null ? null : visit.getFlat().getId(), visit.getHostResident() == null ? null : visit.getHostResident().getId(), visit.getHandledBySecurity() == null ? null : visit.getHandledBySecurity().getId(), visit.getAccessPass() == null ? null : visit.getAccessPass().getId(), visit.getVisitorType(), visit.getPurpose(), visit.getEntryTime(), visit.getExitTime(), visit.getStatus(), visit.getCreatedAt()); }
}
