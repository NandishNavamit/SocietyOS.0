package com.database.societyproject.security.dto;
import java.time.LocalDateTime;
public record UserBlockAssignmentResponse(Long id, Long userId, Long blockId, Long societyId, Boolean active, LocalDateTime createdAt) { }
