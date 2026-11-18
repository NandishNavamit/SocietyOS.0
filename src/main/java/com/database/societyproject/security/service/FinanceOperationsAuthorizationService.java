package com.database.societyproject.security.service;

import com.database.societyproject.security.entity.UserRole;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Enforces that finance actions are performed within the caller's own society. */
@Service
public class FinanceOperationsAuthorizationService {
    private final SocietyAuthorizationService authorization;
    private final UserSocietyRoleRepository roles;

    public FinanceOperationsAuthorizationService(SocietyAuthorizationService authorization, UserSocietyRoleRepository roles) {
        this.authorization = authorization;
        this.roles = roles;
    }

    public void requireOperator(Long societyId) {
        if (authorization.isAdmin() || authorization.canManage(societyId)) return;
        throw new AccessDeniedException("Society administrator access is required for this society");
    }
}
