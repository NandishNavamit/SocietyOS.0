package com.database.societyproject.resident.dto;

import jakarta.validation.constraints.NotNull;

public record ResidentRequest(@NotNull Long userId, Boolean active) { }
