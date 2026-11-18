package com.database.societyproject.security.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^(?:\\+91[ -]?)?[6-9][0-9]{9}$", message = "Enter a valid 10-digit Indian mobile number") String phoneNumber,
        @NotBlank @Size(min = 8, max = 100) String password) { }
