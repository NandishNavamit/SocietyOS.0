package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record PaymentRequest(@NotNull Long billId, @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                             @NotNull LocalDate paymentDate, @NotNull PaymentMethod paymentMethod,
                             @Size(max = 255) String referenceNumber, @Size(max = 1000) String notes) { }
