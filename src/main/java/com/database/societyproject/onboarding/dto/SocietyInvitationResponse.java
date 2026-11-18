package com.database.societyproject.onboarding.dto;
import com.database.societyproject.onboarding.entity.SocietyInvitationStatus;
import com.database.societyproject.security.entity.UserRole;
import java.time.LocalDateTime;
public record SocietyInvitationResponse(Long id, Long societyId, String societyName, String email, UserRole role,
                                        Long flatId, String flatNumber, SocietyInvitationStatus status,
                                        LocalDateTime expiresAt, LocalDateTime createdAt, String inviteUrl) {}
