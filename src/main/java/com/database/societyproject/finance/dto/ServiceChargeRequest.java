package com.database.societyproject.finance.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ServiceChargeRequest(@NotNull Long complaintId,@NotBlank @Size(max=1000) String description,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull LocalDate dueDate,@Size(max=255) String vendorName,@Size(max=255) String invoiceNumber,@Size(max=2000) String internalNote){}
