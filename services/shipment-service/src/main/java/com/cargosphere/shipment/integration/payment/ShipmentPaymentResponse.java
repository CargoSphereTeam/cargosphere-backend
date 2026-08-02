package com.cargosphere.shipment.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShipmentPaymentResponse(
        Long id,
        Long shipmentId,
        Long userId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String paymentStatus,
        String paymentType,
        String transactionReference,
        LocalDate dueDate,
        LocalDate paidDate,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
