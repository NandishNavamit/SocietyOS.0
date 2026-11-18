package com.database.societyproject.resident.dto;

import java.time.LocalDateTime;
public record ResidentResponse(Long id, Long userId, String fullName, Boolean active,
                               LocalDateTime createdAt) { }
