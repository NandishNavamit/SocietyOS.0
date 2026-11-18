package com.database.societyproject.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnnouncementRequest(
        @NotNull Long societyId,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String message) { }
