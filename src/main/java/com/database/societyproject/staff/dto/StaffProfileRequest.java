package com.database.societyproject.staff.dto;
import jakarta.validation.constraints.*;
public record StaffProfileRequest(@NotBlank @Size(max = 255) String fullName,
                                  @NotBlank @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "must be a valid phone number") String phoneNumber,
                                  @Email @Size(max = 255) String email, Long userId) { }
