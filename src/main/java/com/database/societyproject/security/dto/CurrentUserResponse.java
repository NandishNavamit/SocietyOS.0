package com.database.societyproject.security.dto;

import com.database.societyproject.security.entity.UserRole;
import java.time.LocalDateTime;
import java.util.List;

public record CurrentUserResponse(Long id, String fullName, String email, String phoneNumber,
                                  Boolean active, LocalDateTime joinedAt, List<UserRole> roles) { }
