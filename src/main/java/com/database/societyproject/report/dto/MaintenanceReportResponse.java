package com.database.societyproject.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaintenanceReportResponse(LocalDate from, LocalDate to, long billCount,
                                        BigDecimal totalBilled, BigDecimal totalCollected,
                                        BigDecimal outstandingAmount, long overdueBillCount,
                                        BigDecimal totalExpenses) { }
