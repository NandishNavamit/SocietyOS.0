package com.database.societyproject.security.dto;

import java.time.LocalDateTime;

public record BlockManagerAssignmentResponse(Long id, Long userId, String userName, Long blockId,
                                             String blockName, Boolean active, LocalDateTime createdAt) { }
