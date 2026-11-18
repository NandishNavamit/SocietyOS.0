package com.database.societyproject.finance.dto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public record BillGenerationRequest(@NotNull LocalDate billingPeriodStart) { }
