package com.database.societyproject.security.dto;

import com.database.societyproject.security.entity.UserRole;
import java.time.LocalDateTime;

public record UserRoleAssignmentResponse(Long id, Long userId, Long societyId, UserRole role,
                                         Boolean active, LocalDateTime createdAt) { }
