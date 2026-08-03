package com.cargosphere.shipment.integration.auth;

import java.time.LocalDateTime;

public record AuthUserResponse(
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
