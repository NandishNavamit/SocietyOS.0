package com.database.societyproject.block.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BlockRequest(
        @NotBlank @Size(max = 255) String name,
        @PositiveOrZero Integer totalFloors,
        @PositiveOrZero Integer totalFlats,
        Boolean active,
        @NotNull Long societyId) { }
