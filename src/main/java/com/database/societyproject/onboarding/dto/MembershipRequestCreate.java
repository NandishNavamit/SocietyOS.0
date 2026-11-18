package com.database.societyproject.onboarding.dto;

import com.database.societyproject.security.entity.UserRole;
import jakarta.validation.constraints.*;

public record MembershipRequestCreate(@NotNull Long societyId, @NotNull UserRole requestedRole, @Size(max = 1000) String note) {}
