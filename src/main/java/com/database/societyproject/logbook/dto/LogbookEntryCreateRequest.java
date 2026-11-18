package com.database.societyproject.logbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LogbookEntryCreateRequest(@NotNull Long societyId, Long flatId,
        @NotBlank @Size(max = 160) String visitorName,
        @Size(max = 40) String phoneNumber, @Size(max = 80) String vehicleNumber,
        @NotBlank @Size(max = 500) String purpose) { }
