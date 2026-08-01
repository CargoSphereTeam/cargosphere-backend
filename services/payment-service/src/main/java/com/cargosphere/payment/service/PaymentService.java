package com.cargosphere.payment.service;

import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(
            CreatePaymentRequest request,
            Long userId
    );

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(Long paymentId);

    List<PaymentResponse> getPaymentsByShipmentId(
            Long shipmentId
    );

    List<PaymentResponse> getPaymentsByUserId(
            Long userId
    );

    PaymentResponse updatePaymentStatus(
            Long paymentId,
            UpdatePaymentStatusRequest request
    );

    PaymentResponse refundPayment(
            Long paymentId,
            RefundPaymentRequest request
    );
}