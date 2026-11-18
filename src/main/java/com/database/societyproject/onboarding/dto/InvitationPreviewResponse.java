package com.database.societyproject.onboarding.dto;
import com.database.societyproject.onboarding.entity.SocietyInvitationStatus;
import com.database.societyproject.security.entity.UserRole;
import java.time.LocalDateTime;
public record InvitationPreviewResponse(String societyName, UserRole role, SocietyInvitationStatus status, LocalDateTime expiresAt,
                                        String recipientEmailHint, String flatNumber) {}
