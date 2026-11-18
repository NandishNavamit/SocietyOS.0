package com.database.societyproject.setup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SetupBlockRequest(@NotBlank @Size(max = 255) String name,
                                @PositiveOrZero Integer totalFloors) { }
