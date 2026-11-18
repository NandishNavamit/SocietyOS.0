package com.database.societyproject.generalcomplaint.dto;

import com.database.societyproject.generalcomplaint.entity.GeneralComplaintStatus;
import java.time.LocalDateTime;

public record GeneralComplaintResponse(Long id, Long societyId, String societyName, Long flatId, String flatNumber,
        String ownerName, String title, String description, GeneralComplaintStatus status, String resolutionNote,
        LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime resolvedAt, LocalDateTime closedAt) { }
