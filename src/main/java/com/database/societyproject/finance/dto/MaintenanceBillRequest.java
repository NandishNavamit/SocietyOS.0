package com.database.societyproject.finance.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record MaintenanceBillRequest(@NotNull Long chargePlanId, @NotNull Long flatId, @NotNull LocalDate billingPeriodStart,
                                     @NotNull LocalDate billingPeriodEnd, @NotNull LocalDate dueDate,
                                     @NotNull @DecimalMin(value = "0.01") BigDecimal amount) { }
