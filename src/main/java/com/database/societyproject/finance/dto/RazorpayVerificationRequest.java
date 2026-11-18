package com.database.societyproject.finance.dto;
import jakarta.validation.constraints.NotBlank;
public record RazorpayVerificationRequest(@NotBlank String razorpayOrderId, @NotBlank String razorpayPaymentId, @NotBlank String razorpaySignature) { }
