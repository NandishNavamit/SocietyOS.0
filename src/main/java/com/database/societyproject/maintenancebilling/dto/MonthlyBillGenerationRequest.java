package com.database.societyproject.maintenancebilling.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
public record MonthlyBillGenerationRequest(@NotNull LocalDate billingMonth) { }
