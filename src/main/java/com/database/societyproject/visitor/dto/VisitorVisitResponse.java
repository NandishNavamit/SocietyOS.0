package com.database.societyproject.visitor.dto;
import com.database.societyproject.visitor.entity.*;
import java.time.*;
public record VisitorVisitResponse(Long id, Long visitorId, Long societyId, Long flatId, Long hostResidentId, Long securityAssignmentId, Long accessPassId, VisitorType visitorType, String purpose, LocalDateTime entryTime, LocalDateTime exitTime, VisitStatus status, LocalDateTime createdAt) { }
