package com.database.societyproject.security.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) { }
