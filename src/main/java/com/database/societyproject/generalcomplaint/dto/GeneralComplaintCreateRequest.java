package com.database.societyproject.generalcomplaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GeneralComplaintCreateRequest(
        @NotNull Long flatId,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 3000) String description) { }
