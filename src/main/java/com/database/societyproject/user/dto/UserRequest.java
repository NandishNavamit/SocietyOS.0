package com.database.societyproject.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(max = 255) String fullName,
        @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[0-9+() -]{7,20}$", message = "must be a valid phone number") String phoneNumber,
        Boolean active) { }
