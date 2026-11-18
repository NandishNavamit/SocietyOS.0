package com.database.societyproject.security.service;

import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.notification.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SocietyAuthorizationService {
    private final UserSocietyRoleRepository roleRepository;
    private final NotificationRepository notificationRepository;
    public SocietyAuthorizationService(UserSocietyRoleRepository roleRepository, NotificationRepository notificationRepository) { this.roleRepository = roleRepository; this.notificationRepository = notificationRepository; }

    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Authentication is required");
        }
        return principal.userId();
    }

    public boolean isAdmin() {
        return roleRepository.existsByUserIdAndRoleAndActiveTrue(currentUserId(), UserRole.ADMIN);
    }

    public void requireAdmin() {
        if (!isAdmin()) throw new AccessDeniedException("Administrator access is required");
    }

    public void requireManager(Long societyId) {
        if (isAdmin()) return;
        if (!roleRepository.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(currentUserId(), societyId, UserRole.SOCIETY_ADMIN)) {
            throw new AccessDeniedException("Society administrator access is required for this society");
        }
    }

    public boolean canManage(Long societyId) {
        try {
            return isAdmin() || roleRepository.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(currentUserId(), societyId, UserRole.SOCIETY_ADMIN);
        } catch (AccessDeniedException exception) {
            return false;
        }
    }

    /**
     * Legacy compatibility only. Block-manager authority is retired and can no
     * longer grant access while the obsolete complaint API is being removed.
     */
    @Deprecated
    public boolean canManageBlock(Long blockId) { return false; }

    @Deprecated
    public void requireBlockManager(Long blockId) {
        throw new AccessDeniedException("Block manager access has been retired");
    }

    public boolean canAccess(Long societyId) {
        try {
            requireSocietyMember(societyId);
            return true;
        } catch (AccessDeniedException exception) {
            return false;
        }
    }

    public boolean canReadNotification(Long notificationId) {
        try {
            return notificationRepository.findById(notificationId).map(notification ->
                    notification.getRecipientUser().getId().equals(currentUserId()))
                    .orElse(false);
        } catch (AccessDeniedException exception) {
            return false;
        }
    }

    public void requireSocietyMember(Long societyId) {
        if (isAdmin()) return;
        Long userId = currentUserId();
        for (UserRole role : new UserRole[] {UserRole.SOCIETY_ADMIN, UserRole.FLAT_OWNER, UserRole.LOGBOOK_OPERATOR}) {
            if (roleRepository.existsByUserIdAndSocietyIdAndRoleAndActiveTrue(userId, societyId, role)) return;
        }
        throw new AccessDeniedException("You do not have access to this society");
    }
}
