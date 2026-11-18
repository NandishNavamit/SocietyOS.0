package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public record SocietyExpenseResponse(Long id, Long societyId, ExpenseCategory category, String description, BigDecimal amount,
                                     LocalDate expenseDate, String vendorName, String referenceNumber, Boolean active, LocalDateTime createdAt) { }
