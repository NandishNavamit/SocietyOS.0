package com.database.societyproject.security.dto;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(@NotBlank String currentPassword,
                                    @NotBlank @Size(min = 8, max = 100) String newPassword) { }
