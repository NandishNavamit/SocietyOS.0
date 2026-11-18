package com.database.societyproject.staff.service;

import com.database.societyproject.society.entity.Society;
import com.database.societyproject.staff.entity.StaffAssignment;
import com.database.societyproject.staff.entity.StaffProfile;
import com.database.societyproject.common.exception.DuplicateResourceException;
import com.database.societyproject.common.exception.ResourceNotFoundException;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.staff.dto.StaffAssignmentRequest;
import com.database.societyproject.staff.dto.StaffAssignmentResponse;
import com.database.societyproject.staff.repository.StaffAssignmentRepository;
import com.database.societyproject.security.service.SecurityOperationsAuthorizationService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class StaffAssignmentService {
    private final StaffAssignmentRepository repository;
    private final StaffProfileService profileService;
    private final SocietyService societyService;
    private final SecurityOperationsAuthorizationService securityAccess;
    public StaffAssignmentService(StaffAssignmentRepository repository, StaffProfileService profileService, SocietyService societyService, SecurityOperationsAuthorizationService securityAccess) {
        this.repository = repository; this.profileService = profileService; this.societyService = societyService; this.securityAccess = securityAccess;
    }
    public StaffAssignmentResponse create(StaffAssignmentRequest request) { ensureUnique(request.staffProfileId(), request.societyId(), null); StaffAssignment assignment = new StaffAssignment(); apply(request, assignment); return toResponse(repository.save(assignment)); }
    @Transactional(readOnly = true) public List<StaffAssignmentResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public StaffAssignmentResponse findById(Long id) { return toResponse(findEntity(id)); }
    @Transactional(readOnly = true) public List<StaffAssignmentResponse> findBySocietyId(Long societyId) { societyService.findEntity(societyId); return repository.findBySocietyId(societyId).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<StaffAssignmentResponse> findSecurityContextBySocietyId(Long societyId) { securityAccess.requireOperator(societyId); return repository.findBySocietyIdAndStaffTypeAndActiveTrue(societyId, com.database.societyproject.staff.entity.StaffType.SECURITY_GUARD).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true) public List<StaffAssignmentResponse> findByProfileId(Long profileId) { profileService.findEntity(profileId); return repository.findByStaffProfileId(profileId).stream().map(this::toResponse).toList(); }
    public StaffAssignmentResponse update(Long id, StaffAssignmentRequest request) { StaffAssignment assignment = findEntity(id); ensureUnique(request.staffProfileId(), request.societyId(), id); apply(request, assignment); return toResponse(assignment); }
    public void deactivate(Long id) { findEntity(id).setActive(false); }
    public StaffAssignment findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff assignment", id)); }
    private void ensureUnique(Long profileId, Long societyId, Long id) {
        boolean exists = id == null ? repository.existsByStaffProfileIdAndSocietyId(profileId, societyId) : repository.existsByStaffProfileIdAndSocietyIdAndIdNot(profileId, societyId, id);
        if (exists) throw new DuplicateResourceException("This staff profile already has an assignment for this society");
    }
    private void apply(StaffAssignmentRequest request, StaffAssignment assignment) {
        StaffProfile profile = profileService.findEntity(request.staffProfileId()); Society society = societyService.findEntity(request.societyId());
        assignment.setStaffProfile(profile); assignment.setSociety(society); assignment.setStaffType(request.staffType()); assignment.setJoiningDate(request.joiningDate());
        if (request.active() != null) assignment.setActive(request.active());
    }
    private StaffAssignmentResponse toResponse(StaffAssignment entity) { return new StaffAssignmentResponse(entity.getId(), entity.getStaffProfile().getId(), entity.getStaffProfile().getUser() == null ? null : entity.getStaffProfile().getUser().getId(), entity.getStaffProfile().getFullName(), entity.getSociety().getId(), entity.getStaffType(), entity.getJoiningDate(), entity.getActive(), entity.getCreatedAt()); }
}
