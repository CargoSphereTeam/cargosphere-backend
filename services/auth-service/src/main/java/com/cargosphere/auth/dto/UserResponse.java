package com.cargosphere.auth.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}