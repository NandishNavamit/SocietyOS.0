package com.database.societyproject.finance.dto;
public record GatewayOrderResponse(Long id, Long billId, String razorpayOrderId, long amountPaise, String currency, String keyId) { }
