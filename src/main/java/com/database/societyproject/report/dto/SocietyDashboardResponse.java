package com.database.societyproject.report.dto;

import java.math.BigDecimal;

public record SocietyDashboardResponse(
        Long societyId, String societyName,
        long activeBlocks, long activeFlats, long occupiedFlats, long vacantFlats, long activeResidents,
        long activeStaff,
        BigDecimal totalBilled, BigDecimal totalCollected, BigDecimal outstandingAmount,
        long overdueBillCount, BigDecimal activeExpenseTotal,
        long openComplaints, long assignedComplaints, long inProgressComplaints,
        long resolvedComplaints, long closedComplaints,
        long visitsToday, long checkedInVisitors, long unreadNotifications) { }
