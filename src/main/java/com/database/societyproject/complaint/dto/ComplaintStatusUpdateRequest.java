package com.database.societyproject.complaint.dto;
import com.database.societyproject.complaint.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record ComplaintStatusUpdateRequest(@NotNull ComplaintStatus status, @Size(max = 1000) String note) { }
