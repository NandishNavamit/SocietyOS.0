package com.database.societyproject.maintenancepolicy.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenancePolicyRequestCreate(
        @NotNull Long societyId,
        @NotNull @DecimalMin(value = "1.00") BigDecimal monthlyAmount,
        @NotNull @Min(1) @Max(31) Integer dueDayOfMonth,
        @NotNull @DecimalMin(value = "0.00") BigDecimal lateFeePerOverdueMonth,
        @NotNull LocalDate effectiveMonth) { }
