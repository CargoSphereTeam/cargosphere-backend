package com.cargosphere.payment.audit;

import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentAuditEventFactoryTest {

    private PaymentAuditEventFactory factory;

    @BeforeEach
    void setUp() {
        factory =
                new PaymentAuditEventFactory();
    }

    @Test
    void paymentCreatedShouldCreatePendingEvent() {
        Payment payment =
                payment(PaymentStatus.PENDING);

        AuditEventRequest event =
                factory.paymentCreated(
                        payment,
                        clientActor()
                );

        assertEquals(
                "PAYMENT_CREATED",
                event.action()
        );

        assertEquals(
                "PAYMENT",
                event.entityType()
        );

        assertEquals(
                "55",
                event.entityId()
        );

        assertEquals(
                "payment-service",
                event.serviceName()
        );

        assertEquals(
                "ROLE_CLIENT",
                event.actorRole()
        );

        assertEquals(
                "POST",
                event.httpMethod()
        );

        assertEquals(
                "/api/payments",
                event.endpoint()
        );

        assertEquals(
                201,
                event.statusCode()
        );
    }

    @Test
    void paidStatusShouldCreateCompletedEvent() {
        Payment payment =
                payment(PaymentStatus.PAID);

        AuditEventRequest event =
                factory.paymentStatusUpdated(
                        payment,
                        PaymentStatus.PENDING,
                        adminActor()
                );

        assertEquals(
                "PAYMENT_COMPLETED",
                event.action()
        );

        assertEquals(
                "Payment status changed from PENDING to PAID",
                event.description()
        );

        assertEquals(
                "PATCH",
                event.httpMethod()
        );

        assertEquals(
                "/api/payments/55/status",
                event.endpoint()
        );

        assertEquals(
                200,
                event.statusCode()
        );
    }

    @Test
    void failedStatusShouldCreateGenericStatusEvent() {
        Payment payment =
                payment(PaymentStatus.FAILED);

        AuditEventRequest event =
                factory.paymentStatusUpdated(
                        payment,
                        PaymentStatus.PENDING,
                        adminActor()
                );

        assertEquals(
                "PAYMENT_STATUS_UPDATED",
                event.action()
        );

        assertEquals(
                "Payment status changed from PENDING to FAILED",
                event.description()
        );
    }

    @Test
    void refundShouldCreateRefundEvent() {
        Payment payment =
                payment(PaymentStatus.REFUNDED);

        AuditEventRequest event =
                factory.paymentRefunded(
                        payment,
                        PaymentStatus.PAID,
                        adminActor()
                );

        assertEquals(
                "PAYMENT_REFUNDED",
                event.action()
        );

        assertEquals(
                "Payment refunded successfully from status PAID",
                event.description()
        );

        assertEquals(
                "POST",
                event.httpMethod()
        );

        assertEquals(
                "/api/payments/55/refund",
                event.endpoint()
        );
    }

    private Payment payment(
            PaymentStatus status
    ) {
        return Payment.builder()
                .id(55L)
                .shipmentId(1001L)
                .userId(10L)
                .paymentStatus(status)
                .build();
    }

    private CurrentActor clientActor() {
        return new CurrentActor(
                10L,
                "ROLE_CLIENT"
        );
    }

    private CurrentActor adminActor() {
        return new CurrentActor(
                1L,
                "ROLE_ADMIN"
        );
    }
}