package com.database.societyproject.staff.dto;
import java.time.LocalDateTime;
public record StaffProfileResponse(Long id, String fullName, String phoneNumber, String email, Long userId, LocalDateTime createdAt) { }
