package com.database.societyproject.maintenancepayment.dto;
import com.database.societyproject.finance.entity.PaymentMethod; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDate;
public record OfflinePaymentRequest(@NotNull Long billId,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull LocalDate paymentDate,@NotNull PaymentMethod paymentMethod,@NotBlank @Size(max=255) String referenceNumber,@Size(max=1000) String notes) { }
