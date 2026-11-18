package com.database.societyproject.complaint.dto;
import com.database.societyproject.complaint.entity.ComplaintStatus;
import java.time.LocalDateTime;
public record ComplaintStatusHistoryResponse(Long id, ComplaintStatus previousStatus, ComplaintStatus newStatus, String note, LocalDateTime changedAt) { }
