package com.database.societyproject.complaint.dto;
import com.database.societyproject.complaint.entity.*;
import jakarta.validation.constraints.*;
public record ComplaintRequest(@NotNull Long societyId, Long flatId, @NotNull Long raisedByResidentId,
                               @NotNull ComplaintCategory category, @NotBlank @Size(max = 255) String title,
                               @NotBlank @Size(max = 2000) String description, @NotNull ComplaintPriority priority) { }
