package com.database.societyproject.complaint.dto;
import com.database.societyproject.complaint.entity.*;
import java.time.LocalDateTime;
public record ComplaintResponse(Long id, Long societyId, Long flatId, Long raisedByResidentId, Long assignedStaffAssignmentId,
                                ComplaintCategory category, String title, String description, ComplaintPriority priority,
                                ComplaintStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                                LocalDateTime resolvedAt, LocalDateTime closedAt) { }
