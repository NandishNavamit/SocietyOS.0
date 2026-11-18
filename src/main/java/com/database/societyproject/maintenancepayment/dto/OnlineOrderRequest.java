package com.database.societyproject.maintenancepayment.dto;
import jakarta.validation.constraints.*; import java.util.List;
public record OnlineOrderRequest(@NotEmpty @Size(max=24) List<@NotNull Long> billIds) { }
