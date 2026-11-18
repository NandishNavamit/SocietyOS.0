package com.database.societyproject.security.service;

import com.database.societyproject.common.exception.InvalidRequestException;
import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.staff.entity.StaffAssignment;
import com.database.societyproject.staff.entity.StaffType;
import com.database.societyproject.staff.repository.StaffAssignmentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Applies the same society-bound rules to every visitor and access-pass operation. */
@Service
public class SecurityOperationsAuthorizationService {
    private final SocietyAuthorizationService authorization;
    private final UserSocietyRoleRepository roles;
    private final StaffAssignmentRepository assignments;

    public SecurityOperationsAuthorizationService(SocietyAuthorizationService authorization,
            UserSocietyRoleRepository roles, StaffAssignmentRepository assignments) {
        this.authorization = authorization;
        this.roles = roles;
        this.assignments = assignments;
    }

    public void requireSupervisor(Long societyId) {
        if (authorization.isAdmin() || authorization.canManage(societyId)
                || roles.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(authorization.currentUserId(), societyId, UserRole.SECURITY_MANAGER)) return;
        throw new AccessDeniedException("Security manager access is required for this society");
    }

    public void requireOperator(Long societyId) {
        if (isSupervisor(societyId)) return;
        currentGuardAssignment(societyId);
    }

    public StaffAssignment handlingAssignment(Long societyId, Long requestedAssignmentId) {
        if (!isSupervisor(societyId)) return currentGuardAssignment(societyId);
        if (requestedAssignmentId == null) return null;
        StaffAssignment assignment = assignments.findById(requestedAssignmentId)
                .orElseThrow(() -> new InvalidRequestException("Security assignment was not found"));
        if (!Boolean.TRUE.equals(assignment.getActive()) || assignment.getStaffType() != StaffType.SECURITY_GUARD
                || !assignment.getSociety().getId().equals(societyId)) {
            throw new InvalidRequestException("Security handler must be an active security guard assigned to this society");
        }
        return assignment;
    }

    public boolean isSupervisor(Long societyId) {
        return authorization.isAdmin() || authorization.canManage(societyId)
                || roles.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(authorization.currentUserId(), societyId, UserRole.SECURITY_MANAGER);
    }

    private StaffAssignment currentGuardAssignment(Long societyId) {
        Long userId = authorization.currentUserId();
        if (!roles.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(userId, societyId, UserRole.SECURITY)) {
            throw new AccessDeniedException("Security access is required for this society");
        }
        return assignments.findByStaffProfileUserIdAndSocietyIdAndStaffTypeAndActiveTrue(userId, societyId, StaffType.SECURITY_GUARD)
                .orElseThrow(() -> new AccessDeniedException("An active security-guard assignment is required for this society"));
    }
}
