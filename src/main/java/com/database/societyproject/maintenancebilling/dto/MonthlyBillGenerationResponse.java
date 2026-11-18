package com.database.societyproject.maintenancebilling.dto;

import java.time.LocalDate;
public record MonthlyBillGenerationResponse(Long societyId, LocalDate billingMonth, int generatedCount, int existingCount) { }
