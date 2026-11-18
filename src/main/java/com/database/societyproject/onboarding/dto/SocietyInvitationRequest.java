package com.database.societyproject.onboarding.dto;
import com.database.societyproject.security.entity.UserRole;
import jakarta.validation.constraints.*;
public record SocietyInvitationRequest(@NotNull Long societyId, @NotBlank @Email @Size(max=255) String email,
                                      @NotNull UserRole role, Long flatId) {}
