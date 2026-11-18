package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.ChargeFrequency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ChargePlanRequest(@NotNull Long societyId, @NotBlank @Size(max = 255) String name,
                                @NotNull @DecimalMin(value = "0.01") BigDecimal amount, @NotNull ChargeFrequency frequency,
                                @Min(1) @Max(31) Integer dueDayOfMonth, Boolean active) { }
