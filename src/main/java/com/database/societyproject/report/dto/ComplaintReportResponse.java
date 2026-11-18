package com.database.societyproject.report.dto;

import com.database.societyproject.complaint.entity.ComplaintStatus;
import java.util.Map;

public record ComplaintReportResponse(long totalComplaints, Map<ComplaintStatus, Long> countsByStatus) { }
