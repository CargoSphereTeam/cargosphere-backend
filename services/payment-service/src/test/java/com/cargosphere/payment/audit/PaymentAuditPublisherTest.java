package com.cargosphere.payment.audit;

import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAuditPublisherTest {

    @Mock
    private AuditClient auditClient;

    @Mock
    private PaymentAuditEventFactory
            paymentAuditEventFactory;

    @Mock
    private PaymentActorProvider
            paymentActorProvider;

    @InjectMocks
    private PaymentAuditPublisher
            paymentAuditPublisher;

    @Test
    void paymentCreatedShouldPublishEvent() {
        Payment payment =
                payment(PaymentStatus.PENDING);

        CurrentActor actor =
                new CurrentActor(
                        10L,
                        "ROLE_CLIENT"
                );

        AuditEventRequest event =
                event("PAYMENT_CREATED");

        when(paymentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(paymentAuditEventFactory
                .paymentCreated(
                        payment,
                        actor
                ))
                .thenReturn(event);

        paymentAuditPublisher
                .publishPaymentCreated(payment);

        verify(auditClient)
                .publish(event);
    }

    @Test
    void paidStatusShouldPublishCompletedEvent() {
        Payment payment =
                payment(PaymentStatus.PAID);

        CurrentActor actor =
                new CurrentActor(
                        1L,
                        "ROLE_ADMIN"
                );

        AuditEventRequest event =
                event("PAYMENT_COMPLETED");

        when(paymentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(paymentAuditEventFactory
                .paymentStatusUpdated(
                        payment,
                        PaymentStatus.PENDING,
                        actor
                ))
                .thenReturn(event);

        paymentAuditPublisher
                .publishPaymentStatusUpdated(
                        payment,
                        PaymentStatus.PENDING
                );

        verify(auditClient)
                .publish(event);
    }

    @Test
    void refundShouldPublishRefundEvent() {
        Payment payment =
                payment(PaymentStatus.REFUNDED);

        CurrentActor actor =
                new CurrentActor(
                        1L,
                        "ROLE_ADMIN"
                );

        AuditEventRequest event =
                event("PAYMENT_REFUNDED");

        when(paymentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(paymentAuditEventFactory
                .paymentRefunded(
                        payment,
                        PaymentStatus.PAID,
                        actor
                ))
                .thenReturn(event);

        paymentAuditPublisher
                .publishPaymentRefunded(
                        payment,
                        PaymentStatus.PAID
                );

        verify(auditClient)
                .publish(event);
    }

    @Test
    void unexpectedAuditFailureShouldNotEscape() {
        Payment payment =
                payment(PaymentStatus.PENDING);

        CurrentActor actor =
                new CurrentActor(
                        10L,
                        "ROLE_CLIENT"
                );

        AuditEventRequest event =
                event("PAYMENT_CREATED");

        when(paymentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(paymentAuditEventFactory
                .paymentCreated(
                        payment,
                        actor
                ))
                .thenReturn(event);

        doThrow(
                new RuntimeException(
                        "Audit unavailable"
                )
        )
                .when(auditClient)
                .publish(event);

        assertDoesNotThrow(() ->
                paymentAuditPublisher
                        .publishPaymentCreated(
                                payment
                        )
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

    private AuditEventRequest event(
            String action
    ) {
        return new AuditEventRequest(
                10L,
                "ROLE_CLIENT",
                action,
                "PAYMENT",
                "55",
                "payment-service",
                "Payment audit event",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/payments",
                201
        );
    }
}