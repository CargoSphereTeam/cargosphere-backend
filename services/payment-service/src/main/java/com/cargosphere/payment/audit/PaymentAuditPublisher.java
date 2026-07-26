package com.cargosphere.payment.audit;

import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentAuditPublisher {

    private final AuditClient auditClient;

    private final PaymentAuditEventFactory
            paymentAuditEventFactory;

    private final PaymentActorProvider
            paymentActorProvider;

    public void publishPaymentCreated(
            Payment payment
    ) {
        try {
            CurrentActor actor =
                    paymentActorProvider
                            .getCurrentActor();

            AuditEventRequest event =
                    paymentAuditEventFactory
                            .paymentCreated(
                                    payment,
                                    actor
                            );

            auditClient.publish(event);

        } catch (Exception exception) {
            logFailure(
                    "PAYMENT_CREATED",
                    payment,
                    exception
            );
        }
    }

    public void publishPaymentStatusUpdated(
            Payment payment,
            PaymentStatus previousStatus
    ) {
        try {
            CurrentActor actor =
                    paymentActorProvider
                            .getCurrentActor();

            AuditEventRequest event =
                    paymentAuditEventFactory
                            .paymentStatusUpdated(
                                    payment,
                                    previousStatus,
                                    actor
                            );

            auditClient.publish(event);

        } catch (Exception exception) {
            String action =
                    payment != null
                            && payment.getPaymentStatus()
                            == PaymentStatus.PAID
                            ? "PAYMENT_COMPLETED"
                            : "PAYMENT_STATUS_UPDATED";

            logFailure(
                    action,
                    payment,
                    exception
            );
        }
    }

    public void publishPaymentRefunded(
            Payment payment,
            PaymentStatus previousStatus
    ) {
        try {
            CurrentActor actor =
                    paymentActorProvider
                            .getCurrentActor();

            AuditEventRequest event =
                    paymentAuditEventFactory
                            .paymentRefunded(
                                    payment,
                                    previousStatus,
                                    actor
                            );

            auditClient.publish(event);

        } catch (Exception exception) {
            logFailure(
                    "PAYMENT_REFUNDED",
                    payment,
                    exception
            );
        }
    }

    private void logFailure(
            String action,
            Payment payment,
            Exception exception
    ) {
        Long paymentId =
                payment == null
                        ? null
                        : payment.getId();

        log.warn(
                "Payment audit publication failed: action={}, paymentId={}, cause={}",
                action,
                paymentId,
                exception
                        .getClass()
                        .getSimpleName()
        );
    }
}