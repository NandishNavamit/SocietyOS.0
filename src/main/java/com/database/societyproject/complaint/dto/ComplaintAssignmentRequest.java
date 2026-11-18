package com.database.societyproject.complaint.dto;
import jakarta.validation.constraints.NotNull;
public record ComplaintAssignmentRequest(@NotNull Long staffAssignmentId, String note) { }
