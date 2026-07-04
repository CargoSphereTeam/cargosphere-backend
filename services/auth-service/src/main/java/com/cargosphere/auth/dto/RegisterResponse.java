package com.cargosphere.auth.dto;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt
) {
}