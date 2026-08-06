package com.cargosphere.payment.service;

import com.cargosphere.payment.dto.RazorpayOrderResponse;
import com.cargosphere.payment.dto.RazorpayVerificationRequest;
import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;

public interface RazorpayCheckoutService {

    RazorpayOrderResponse createOrder(Long shipmentId, Long userId);

    ShipmentPaymentSummaryResponse verifyPayment(
            Long shipmentId,
            RazorpayVerificationRequest request,
            Long userId
    );
}
