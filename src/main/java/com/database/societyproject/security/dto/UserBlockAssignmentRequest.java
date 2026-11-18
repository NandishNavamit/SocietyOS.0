package com.database.societyproject.security.dto;
import jakarta.validation.constraints.NotNull;
public record UserBlockAssignmentRequest(@NotNull Long userId, @NotNull Long blockId) { }
