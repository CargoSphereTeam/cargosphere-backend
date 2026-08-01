package com.cargosphere.payment.mapper;

import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    @Test
    void toEntityShouldMapRequestAndNormalizeValues() {
        CreatePaymentRequest request =
                CreatePaymentRequest.builder()
                        .shipmentId(1001L)
                        .amount(new BigDecimal("2500.50"))
                        .currency(" inr ")
                        .paymentMethod(PaymentMethod.UPI)
                        .paymentType(PaymentType.FULL)
                        .transactionReference(" TXN-1001 ")
                        .dueDate(LocalDate.of(2026, 8, 1))
                        .remarks(" Freight payment ")
                        .build();

        Payment payment =
                paymentMapper.toEntity(request, 10L);

        assertEquals(1001L, payment.getShipmentId());
        assertEquals(10L, payment.getUserId());
        assertEquals(
                new BigDecimal("2500.50"),
                payment.getAmount()
        );
        assertEquals("INR", payment.getCurrency());
        assertEquals(
                PaymentMethod.UPI,
                payment.getPaymentMethod()
        );
        assertEquals(
                PaymentStatus.PENDING,
                payment.getPaymentStatus()
        );
        assertEquals(
                PaymentType.FULL,
                payment.getPaymentType()
        );
        assertEquals(
                "TXN-1001",
                payment.getTransactionReference()
        );
        assertEquals(
                LocalDate.of(2026, 8, 1),
                payment.getDueDate()
        );
        assertEquals(
                "Freight payment",
                payment.getRemarks()
        );
        assertNull(payment.getPaidDate());
    }

    @Test
    void toResponseShouldMapAllPaymentFields() {
        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        24,
                        10,
                        30
                );

        LocalDateTime updatedAt =
                LocalDateTime.of(
                        2026,
                        7,
                        24,
                        11,
                        0
                );

        Payment payment = Payment.builder()
                .id(1L)
                .shipmentId(1001L)
                .userId(10L)
                .amount(new BigDecimal("2500.50"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.PAID)
                .paymentType(PaymentType.FULL)
                .transactionReference("TXN-1001")
                .dueDate(LocalDate.of(2026, 8, 1))
                .paidDate(LocalDate.of(2026, 7, 24))
                .remarks("Payment completed")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        PaymentResponse response =
                paymentMapper.toResponse(payment);

        assertEquals(1L, response.getId());
        assertEquals(1001L, response.getShipmentId());
        assertEquals(10L, response.getUserId());
        assertEquals(
                new BigDecimal("2500.50"),
                response.getAmount()
        );
        assertEquals("INR", response.getCurrency());
        assertEquals(
                PaymentMethod.UPI,
                response.getPaymentMethod()
        );
        assertEquals(
                PaymentStatus.PAID,
                response.getPaymentStatus()
        );
        assertEquals(
                PaymentType.FULL,
                response.getPaymentType()
        );
        assertEquals(
                "TXN-1001",
                response.getTransactionReference()
        );
        assertEquals(
                LocalDate.of(2026, 7, 24),
                response.getPaidDate()
        );
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}