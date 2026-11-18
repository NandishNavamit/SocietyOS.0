package com.database.societyproject.security.dto;

import com.database.societyproject.security.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleAssignmentRequest(Long societyId, @NotNull UserRole role) { }
