package com.database.societyproject.visitor.dto;
import com.database.societyproject.visitor.entity.VisitorType;
import jakarta.validation.constraints.*;
public record VisitorCheckInRequest(@NotNull Long visitorId, @NotNull Long societyId, Long flatId, Long hostResidentId, Long securityAssignmentId, Long accessPassId, @NotNull VisitorType visitorType, @NotBlank @Size(max = 1000) String purpose) { }
