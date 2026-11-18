package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record MaintenanceBillResponse(Long id, Long chargePlanId, String chargeName, String chargeDescription, Long flatId, String flatNumber,
                                      LocalDate billingPeriodStart, LocalDate billingPeriodEnd, LocalDate dueDate,
                                      BigDecimal amount, BigDecimal paidAmount, BillStatus status, LocalDateTime createdAt) { }
