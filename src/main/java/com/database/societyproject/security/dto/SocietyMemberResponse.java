package com.database.societyproject.security.dto;

import com.database.societyproject.security.entity.UserRole;
import java.util.List;

public record SocietyMemberResponse(Long userId, String fullName, String email, List<UserRole> roles) { }
