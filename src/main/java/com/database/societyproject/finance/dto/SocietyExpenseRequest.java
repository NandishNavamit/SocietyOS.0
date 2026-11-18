package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.ExpenseCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record SocietyExpenseRequest(@NotNull Long societyId, @NotNull ExpenseCategory category,
                                    @NotBlank @Size(max = 1000) String description, @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                    @NotNull LocalDate expenseDate, @Size(max = 255) String vendorName, @Size(max = 255) String referenceNumber, Boolean active) { }
