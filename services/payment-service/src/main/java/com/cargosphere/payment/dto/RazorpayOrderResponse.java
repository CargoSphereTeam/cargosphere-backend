package com.cargosphere.payment.dto;

public record RazorpayOrderResponse(
        String keyId,
        String orderId,
        long amount,
        String currency,
        Long shipmentId,
        String description
) {
}
