package com.database.societyproject.maintenancebilling.dto;

import com.database.societyproject.finance.entity.BillStatus;
import java.math.BigDecimal;
import java.time.*;
public record MonthlyMaintenanceBillResponse(Long id, Long societyId, Long flatId, String flatNumber, LocalDate billingMonth,
        LocalDate dueDate, BigDecimal maintenanceAmount, BigDecimal lateFeePerOverdueMonth, Integer lateFeeMonthsApplied,
        BigDecimal lateFeeAmount, BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal outstandingAmount,
        BillStatus status, LocalDateTime generatedAt) { }
