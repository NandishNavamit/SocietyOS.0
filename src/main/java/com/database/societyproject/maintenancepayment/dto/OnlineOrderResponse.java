package com.database.societyproject.maintenancepayment.dto;
public record OnlineOrderResponse(Long paymentId, String razorpayOrderId, long amountPaise, String currency, String keyId) { }
