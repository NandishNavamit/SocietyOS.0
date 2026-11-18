package com.database.societyproject.maintenancepolicy.dto;

import com.database.societyproject.maintenancepolicy.entity.MaintenancePolicyStatus;
import java.math.BigDecimal;
import java.time.*;

public record MaintenancePolicyResponse(Long id, Long societyId, String societyName, BigDecimal monthlyAmount,
        Integer dueDayOfMonth, BigDecimal lateFeePerOverdueMonth, LocalDate effectiveMonth,
        MaintenancePolicyStatus status, String submittedByName, String reviewedByName, String reviewNote,
        LocalDateTime submittedAt, LocalDateTime reviewedAt) { }
