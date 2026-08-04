package com.cargosphere.shipment.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipmentPaymentSummaryResponse(
        Long id,
        Long shipmentId,
        BigDecimal estimatedAmount,
        BigDecimal baseAmount,
        BigDecimal charges,
        BigDecimal taxes,
        BigDecimal discount,
        BigDecimal finalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        String currency,
        String paymentMethod,
        String confirmationStatus,
        Long confirmedBy,
        LocalDateTime confirmedAt,
        String remarks,
        boolean paymentConfirmed,
        LocalDateTime updatedAt
) {
}