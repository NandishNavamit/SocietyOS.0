package com.database.societyproject.onboarding.dto;

import jakarta.validation.constraints.*;

public record SocietyRegistrationEnquiryRequest(@NotBlank String contactName, @NotBlank @Email String email, @NotBlank String phoneNumber, @NotBlank String societyName, String city, @PositiveOrZero Integer estimatedBlocks, @PositiveOrZero Integer estimatedFlats, @Size(max = 2000) String message) {}
