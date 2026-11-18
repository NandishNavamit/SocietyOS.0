package com.database.societyproject.report.dto;

import java.time.LocalDate;

public record VisitorReportResponse(LocalDate from, LocalDate to, long totalVisits,
                                    long checkedIn, long checkedOut, long denied) { }
