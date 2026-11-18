package com.database.societyproject.complaint.service;

import com.database.societyproject.complaint.dto.*;
import com.database.societyproject.complaint.entity.*;
import com.database.societyproject.complaint.repository.*;
import com.database.societyproject.common.exception.*;
import com.database.societyproject.flat.entity.Flat;
import com.database.societyproject.flat.service.FlatService;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.resident.entity.Resident;
import com.database.societyproject.resident.service.ResidentService;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.staff.entity.StaffAssignment;
import com.database.societyproject.staff.service.StaffAssignmentService;
import com.database.societyproject.notification.service.NotificationService;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class ComplaintService {
    private final ComplaintRepository repository; private final ComplaintStatusHistoryRepository historyRepository;
    private final SocietyService societyService; private final FlatService flatService; private final ResidentService residentService;
    private final StaffAssignmentService staffService; private final FlatOccupancyRepository occupancyRepository; private final NotificationService notificationService; private final SocietyAuthorizationService authorization;
    public ComplaintService(ComplaintRepository repository, ComplaintStatusHistoryRepository historyRepository, SocietyService societyService, FlatService flatService, ResidentService residentService, StaffAssignmentService staffService, FlatOccupancyRepository occupancyRepository, NotificationService notificationService, SocietyAuthorizationService authorization) {
        this.repository = repository; this.historyRepository = historyRepository; this.societyService = societyService; this.flatService = flatService; this.residentService = residentService; this.staffService = staffService; this.occupancyRepository = occupancyRepository; this.notificationService = notificationService; this.authorization = authorization;
    }
    public ComplaintResponse create(ComplaintRequest request) {
        Society society = societyService.findEntity(request.societyId()); Resident resident = residentService.findEntity(request.raisedByResidentId());
        if (!authorization.canManage(society.getId()) && !resident.getUser().getId().equals(authorization.currentUserId())) throw new org.springframework.security.access.AccessDeniedException("Residents may only create complaints for themselves");
        validateResidentMembership(resident.getId(), society.getId()); Flat flat = validateFlat(request.flatId(), society.getId(), resident.getId());
        Complaint complaint = new Complaint(); complaint.setSociety(society); complaint.setFlat(flat); complaint.setRaisedByResident(resident); complaint.setCategory(request.category()); complaint.setTitle(request.title()); complaint.setDescription(request.description()); complaint.setPriority(request.priority());
        Complaint saved = repository.save(complaint); recordHistory(saved, null, ComplaintStatus.OPEN, "Complaint created"); notificationService.notifyComplaintCreated(saved); return toResponse(saved);
    }
    @Transactional(readOnly = true) public List<ComplaintResponse> findAll() { authorization.requireAdmin(); return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public ComplaintResponse findById(Long id) { Complaint complaint = findEntity(id); requireComplaintAccess(complaint); return toResponse(complaint); }
    @Transactional(readOnly = true) public List<ComplaintResponse> findBySocietyId(Long id) { societyService.findEntity(id); authorization.requireSocietyMember(id); return repository.findBySocietyId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<ComplaintResponse> findByBlockId(Long id) { authorization.requireBlockManager(id); return repository.findByFlatBlockId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<ComplaintResponse> findByResidentId(Long id) { Resident resident = residentService.findEntity(id); boolean manager = occupancyRepository.findByResidentId(id).stream().filter(o -> Boolean.TRUE.equals(o.getActive())).anyMatch(o -> authorization.canManage(o.getFlat().getBlock().getSociety().getId())); if (!manager && !resident.getUser().getId().equals(authorization.currentUserId())) throw new org.springframework.security.access.AccessDeniedException("You may only view your own complaints"); return repository.findByRaisedByResidentId(id).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<ComplaintResponse> findByStatus(ComplaintStatus status) { authorization.requireAdmin(); return repository.findByStatus(status).stream().map(this::toResponse).toList(); }
    public ComplaintResponse assign(Long id, ComplaintAssignmentRequest request) {
        Complaint complaint = findEntity(id); StaffAssignment assignment = staffService.findEntity(request.staffAssignmentId());
        requireComplaintManagement(complaint);
        if (!assignment.getActive()) throw new InvalidRequestException("Cannot assign an inactive staff assignment");
        if (!assignment.getSociety().getId().equals(complaint.getSociety().getId())) throw new InvalidRequestException("Staff assignment must belong to the complaint's society");
        ComplaintStatus previous = complaint.getStatus(); if (previous != ComplaintStatus.OPEN && previous != ComplaintStatus.ASSIGNED) throw new InvalidRequestException("Only open or assigned complaints can be assigned");
        complaint.setAssignedStaffAssignment(assignment); complaint.setStatus(ComplaintStatus.ASSIGNED); recordHistory(complaint, previous, ComplaintStatus.ASSIGNED, request.note()); notificationService.notifyComplaintUpdated(complaint); return toResponse(complaint);
    }
    public ComplaintResponse updateStatus(Long id, ComplaintStatusUpdateRequest request) {
        Complaint complaint = findEntity(id); requireComplaintManagement(complaint); ComplaintStatus previous = complaint.getStatus(); ensureValidTransition(previous, request.status());
        complaint.setStatus(request.status()); if (request.status() == ComplaintStatus.RESOLVED) complaint.setResolvedAt(LocalDateTime.now()); if (request.status() == ComplaintStatus.CLOSED) complaint.setClosedAt(LocalDateTime.now());
        recordHistory(complaint, previous, request.status(), request.note()); notificationService.notifyComplaintUpdated(complaint); return toResponse(complaint);
    }
    @Transactional(readOnly = true) public List<ComplaintStatusHistoryResponse> history(Long id) { Complaint complaint = findEntity(id); requireComplaintAccess(complaint); return historyRepository.findByComplaintIdOrderByChangedAtAsc(id).stream().map(h -> new ComplaintStatusHistoryResponse(h.getId(), h.getPreviousStatus(), h.getNewStatus(), h.getNote(), h.getChangedAt())).toList(); }
    private void requireComplaintAccess(Complaint complaint) { if (!authorization.canManage(complaint.getSociety().getId()) && !canManageComplaintBlock(complaint) && !canWorkComplaint(complaint) && !complaint.getRaisedByResident().getUser().getId().equals(authorization.currentUserId())) throw new org.springframework.security.access.AccessDeniedException("You do not have access to this complaint"); }
    private void requireComplaintManagement(Complaint complaint) { if (authorization.canManage(complaint.getSociety().getId()) || canManageComplaintBlock(complaint) || canWorkComplaint(complaint)) return; throw new org.springframework.security.access.AccessDeniedException("You do not have authority to manage this complaint"); }
    private boolean canManageComplaintBlock(Complaint complaint) { return complaint.getFlat() != null && authorization.canManageBlock(complaint.getFlat().getBlock().getId()); }
    private boolean canWorkComplaint(Complaint complaint) { return complaint.getAssignedStaffAssignment() != null && complaint.getAssignedStaffAssignment().getStaffProfile().getUser() != null && complaint.getAssignedStaffAssignment().getStaffProfile().getUser().getId().equals(authorization.currentUserId()); }
    private Complaint findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Complaint", id)); }
    private void validateResidentMembership(Long residentId, Long societyId) { if (!occupancyRepository.existsByResidentIdAndFlatBlockSocietyIdAndActiveTrue(residentId, societyId)) throw new InvalidRequestException("Resident must have an active occupancy in the complaint's society"); }
    private Flat validateFlat(Long flatId, Long societyId, Long residentId) { if (flatId == null) return null; Flat flat = flatService.findEntity(flatId); if (!flat.getBlock().getSociety().getId().equals(societyId)) throw new InvalidRequestException("Flat must belong to the complaint's society"); if (!occupancyRepository.existsByResidentIdAndFlatIdAndActiveTrue(residentId, flatId)) throw new InvalidRequestException("Resident must have an active occupancy in the specified flat"); return flat; }
    private void ensureValidTransition(ComplaintStatus from, ComplaintStatus to) { boolean valid = (from == ComplaintStatus.ASSIGNED && to == ComplaintStatus.IN_PROGRESS) || (from == ComplaintStatus.IN_PROGRESS && to == ComplaintStatus.RESOLVED) || (from == ComplaintStatus.RESOLVED && to == ComplaintStatus.CLOSED); if (!valid) throw new InvalidRequestException("Invalid complaint status transition from " + from + " to " + to); }
    private void recordHistory(Complaint complaint, ComplaintStatus previous, ComplaintStatus next, String note) { ComplaintStatusHistory history = new ComplaintStatusHistory(); history.setComplaint(complaint); history.setPreviousStatus(previous); history.setNewStatus(next); history.setNote(note); historyRepository.save(history); }
    private ComplaintResponse toResponse(Complaint c) { return new ComplaintResponse(c.getId(), c.getSociety().getId(), c.getFlat() == null ? null : c.getFlat().getId(), c.getRaisedByResident().getId(), c.getAssignedStaffAssignment() == null ? null : c.getAssignedStaffAssignment().getId(), c.getCategory(), c.getTitle(), c.getDescription(), c.getPriority(), c.getStatus(), c.getCreatedAt(), c.getUpdatedAt(), c.getResolvedAt(), c.getClosedAt()); }
}
