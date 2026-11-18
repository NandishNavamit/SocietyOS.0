package com.database.societyproject.setup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetupInviteRequest(@NotBlank @Email @Size(max = 255) String email) { }
