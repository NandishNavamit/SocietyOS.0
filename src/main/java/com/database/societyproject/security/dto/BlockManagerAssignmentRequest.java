package com.database.societyproject.security.dto;

import jakarta.validation.constraints.NotNull;

public record BlockManagerAssignmentRequest(@NotNull Long userId, @NotNull Long blockId) { }
