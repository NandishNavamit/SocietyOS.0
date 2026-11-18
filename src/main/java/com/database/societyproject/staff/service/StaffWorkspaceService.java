package com.database.societyproject.staff.service;

import com.database.societyproject.complaint.dto.ComplaintResponse;
import com.database.societyproject.complaint.entity.Complaint;
import com.database.societyproject.complaint.repository.ComplaintRepository;
import com.database.societyproject.security.service.SocietyAuthorizationService;
import com.database.societyproject.staff.dto.*;
import com.database.societyproject.staff.entity.StaffAssignment;
import com.database.societyproject.staff.repository.StaffProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StaffWorkspaceService {
    private final StaffProfileRepository profiles;
    private final StaffAssignmentService assignments;
    private final ComplaintRepository complaints;
    private final SocietyAuthorizationService authorization;

    public StaffWorkspaceService(StaffProfileRepository profiles, StaffAssignmentService assignments,
                                 ComplaintRepository complaints, SocietyAuthorizationService authorization) {
        this.profiles = profiles; this.assignments = assignments; this.complaints = complaints; this.authorization = authorization;
    }

    public StaffWorkspaceResponse mine() {
        Long userId = authorization.currentUserId();
        var profile = profiles.findByUserId(userId);
        if (profile.isEmpty()) return new StaffWorkspaceResponse(List.of(), List.of());
        List<StaffAssignmentResponse> assignmentResponses = assignments.findByProfileId(profile.get().getId()).stream()
                .filter(StaffAssignmentResponse::active).toList();
        List<ComplaintResponse> complaintResponses = assignmentResponses.stream()
                .flatMap(assignment -> complaints.findByAssignedStaffAssignmentId(assignment.id()).stream())
                .map(this::complaint).toList();
        return new StaffWorkspaceResponse(assignmentResponses, complaintResponses);
    }

    private ComplaintResponse complaint(Complaint c) {
        return new ComplaintResponse(c.getId(), c.getSociety().getId(), c.getFlat() == null ? null : c.getFlat().getId(),
                c.getRaisedByResident().getId(), c.getAssignedStaffAssignment() == null ? null : c.getAssignedStaffAssignment().getId(),
                c.getCategory(), c.getTitle(), c.getDescription(), c.getPriority(), c.getStatus(), c.getCreatedAt(),
                c.getUpdatedAt(), c.getResolvedAt(), c.getClosedAt());
    }
}
