package com.database.societyproject.finance.dto;
import com.database.societyproject.finance.entity.ChargeFrequency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record ChargePlanResponse(Long id,
                                 Long societyId,
                                 String name,
                                 BigDecimal amount,
                                 ChargeFrequency frequency,
                                 Integer dueDayOfMonth,
                                 Boolean active,
                                 LocalDateTime createdAt) { }
