package com.database.societyproject.maintenancepayment.dto;
import jakarta.validation.constraints.NotBlank;
public record OnlineVerificationRequest(@NotBlank String razorpayOrderId,@NotBlank String razorpayPaymentId,@NotBlank String razorpaySignature) { }
