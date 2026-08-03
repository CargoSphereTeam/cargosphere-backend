package com.cargosphere.shipment.dto.ebill.snapshot;

import java.time.LocalDateTime;

public record EbillClientSnapshot(
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
