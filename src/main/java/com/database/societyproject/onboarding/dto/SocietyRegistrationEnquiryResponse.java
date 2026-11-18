package com.database.societyproject.onboarding.dto;

import com.database.societyproject.onboarding.entity.EnquiryStatus;
import java.time.LocalDateTime;

public record SocietyRegistrationEnquiryResponse(Long id, String contactName, String email, String societyName, String city, EnquiryStatus status, LocalDateTime createdAt) {}
