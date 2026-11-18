package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.ServiceChargeStatus;
import java.math.BigDecimal; import java.time.*;
public record ServiceChargeResponse(Long id,Long societyId,Long complaintId,String complaintTitle,Long billId,String flatNumber,String description,BigDecimal amount,LocalDate dueDate,String vendorName,String invoiceNumber,ServiceChargeStatus status,LocalDateTime createdAt){}
