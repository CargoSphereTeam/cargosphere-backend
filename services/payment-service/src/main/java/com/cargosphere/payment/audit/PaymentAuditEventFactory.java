package com.cargosphere.payment.audit;

import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuditEventFactory {

    private static final String ENTITY_TYPE =
            "PAYMENT";

    private static final String SERVICE_NAME =
            "payment-service";

    private static final String SUCCESS =
            "SUCCESS";

    public AuditEventRequest paymentCreated(
            Payment payment,
            CurrentActor actor
    ) {
        return create(
                payment,
                actor,
                "PAYMENT_CREATED",
                "Payment created successfully with status "
                        + payment.getPaymentStatus(),
                "POST",
                "/api/payments",
                201
        );
    }

    public AuditEventRequest paymentStatusUpdated(
            Payment payment,
            PaymentStatus previousStatus,
            CurrentActor actor
    ) {
        String action =
                payment.getPaymentStatus()
                        == PaymentStatus.PAID
                        ? "PAYMENT_COMPLETED"
                        : "PAYMENT_STATUS_UPDATED";

        String description =
                "Payment status changed from "
                        + previousStatus
                        + " to "
                        + payment.getPaymentStatus();

        return create(
                payment,
                actor,
                action,
                description,
                "PATCH",
                "/api/payments/"
                        + payment.getId()
                        + "/status",
                200
        );
    }

    public AuditEventRequest paymentRefunded(
            Payment payment,
            PaymentStatus previousStatus,
            CurrentActor actor
    ) {
        String description =
                "Payment refunded successfully from status "
                        + previousStatus;

        return create(
                payment,
                actor,
                "PAYMENT_REFUNDED",
                description,
                "POST",
                "/api/payments/"
                        + payment.getId()
                        + "/refund",
                200
        );
    }

    private AuditEventRequest create(
            Payment payment,
            CurrentActor actor,
            String action,
            String description,
            String httpMethod,
            String endpoint,
            int statusCode
    ) {
        CurrentActor safeActor =
                actor == null
                        ? CurrentActor.anonymous()
                        : actor;

        return new AuditEventRequest(
                safeActor.userId(),
                safeActor.role(),
                action,
                ENTITY_TYPE,
                payment.getId() == null
                        ? null
                        : payment
                                .getId()
                                .toString(),
                SERVICE_NAME,
                description,
                SUCCESS,
                null,
                null,
                httpMethod,
                endpoint,
                statusCode
        );
    }
}