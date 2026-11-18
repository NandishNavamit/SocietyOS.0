package com.database.societyproject.onboarding.dto;

import com.database.societyproject.onboarding.entity.MembershipRequestStatus;
import com.database.societyproject.security.entity.UserRole;
import java.time.LocalDateTime;

public record MembershipRequestResponse(Long id, Long userId, String userName, String userEmail, Long societyId, UserRole requestedRole, MembershipRequestStatus status, String note, LocalDateTime createdAt) {}
