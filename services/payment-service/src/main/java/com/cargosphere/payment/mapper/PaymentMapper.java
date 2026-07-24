package com.cargosphere.payment.mapper;

import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(
            CreatePaymentRequest request,
            Long userId
    ) {
        return Payment.builder()
                .shipmentId(request.getShipmentId())
                .userId(userId)
                .amount(request.getAmount())
                .currency(normalizeCurrency(
                        request.getCurrency()
                ))
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentType(request.getPaymentType())
                .transactionReference(
                        normalizeNullable(
                                request.getTransactionReference()
                        )
                )
                .dueDate(request.getDueDate())
                .remarks(
                        normalizeNullable(
                                request.getRemarks()
                        )
                )
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .shipmentId(payment.getShipmentId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentType(payment.getPaymentType())
                .transactionReference(
                        payment.getTransactionReference()
                )
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .remarks(payment.getRemarks())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "INR";
        }

        return currency.trim().toUpperCase();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}