package com.database.societyproject.finance.dto;
import jakarta.validation.constraints.NotNull;
public record GatewayOrderRequest(@NotNull Long billId) { }
