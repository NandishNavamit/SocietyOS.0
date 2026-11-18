package com.database.societyproject.visitor.dto;
import com.database.societyproject.visitor.entity.*;
import java.time.*;
public record VisitorAccessPassResponse(Long id, Long visitorId, Long societyId, Long flatId, Long hostResidentId, AccessType accessType, AccessPassStatus status, LocalDate validFrom, LocalDate validUntil, String purpose, LocalDateTime createdAt) { }
