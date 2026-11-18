package com.database.societyproject.report.dto;

import com.database.societyproject.staff.entity.StaffType;
import java.util.Map;

public record StaffReportResponse(long activeStaffCount, Map<StaffType, Long> countsByType) { }
