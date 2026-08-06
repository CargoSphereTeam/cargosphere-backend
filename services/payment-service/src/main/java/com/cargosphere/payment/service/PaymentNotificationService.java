package com.cargosphere.payment.service;

import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;

public interface PaymentNotificationService {

    void sendPaymentRequest(
            Long shipmentId,
            ShipmentPaymentSummaryResponse summary,
            String bearerToken
    );

    void sendPaymentReceipt(
            Long shipmentId,
            ShipmentPaymentSummaryResponse summary,
            String bearerToken
    );
}
