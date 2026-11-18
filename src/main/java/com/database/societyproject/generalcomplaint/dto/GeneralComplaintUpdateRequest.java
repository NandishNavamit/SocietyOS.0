package com.database.societyproject.generalcomplaint.dto;

import com.database.societyproject.generalcomplaint.entity.GeneralComplaintStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GeneralComplaintUpdateRequest(
        @NotNull GeneralComplaintStatus status,
        @Size(max = 3000) String resolutionNote) { }
