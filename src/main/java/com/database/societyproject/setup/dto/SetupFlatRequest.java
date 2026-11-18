package com.database.societyproject.setup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SetupFlatRequest(@NotBlank @Size(max = 255) String flatNumber,
                               @PositiveOrZero Integer floorNumber,
                               @Size(max = 100) String flatType,
                               @PositiveOrZero Double areaSqft) { }
