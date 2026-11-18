package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record PaymentResponse(Long id, Long billId, BigDecimal amount, LocalDate paymentDate, PaymentMethod paymentMethod,
                              String referenceNumber, String notes, PaymentStatus status, LocalDateTime createdAt) { }
