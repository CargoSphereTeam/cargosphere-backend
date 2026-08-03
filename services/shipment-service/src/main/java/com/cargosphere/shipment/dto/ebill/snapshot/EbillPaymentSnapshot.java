package com.cargosphere.shipment.dto.ebill.snapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EbillPaymentSnapshot(
        Long paymentId,
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
