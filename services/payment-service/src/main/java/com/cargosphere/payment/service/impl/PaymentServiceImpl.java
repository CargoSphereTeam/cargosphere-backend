package com.cargosphere.payment.service.impl;

import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;
import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.exception.DuplicateTransactionReferenceException;
import com.cargosphere.payment.exception.InvalidJwtClaimException;
import com.cargosphere.payment.exception.InvalidPaymentStateException;
import com.cargosphere.payment.exception.PaymentNotFoundException;
import com.cargosphere.payment.mapper.PaymentMapper;
import com.cargosphere.payment.repository.PaymentRepository;
import com.cargosphere.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cargosphere.payment.audit.PaymentAuditPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Sort NEWEST_FIRST =
            Sort.by(
                    Sort.Direction.DESC,
                    "createdAt"
            );

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
private final PaymentAuditPublisher
        paymentAuditPublisher;

    @Override
public PaymentResponse createPayment(
        CreatePaymentRequest request,
        Long userId
) {
    validateUserId(userId);

    String transactionReference =
            normalizeNullable(
                    request.getTransactionReference()
            );

    validateTransactionReferenceAvailable(
            transactionReference,
            null
    );

    Payment payment =
            paymentMapper.toEntity(
                    request,
                    userId
            );

    Payment savedPayment =
            paymentRepository.save(payment);

    paymentAuditPublisher
            .publishPaymentCreated(
                    savedPayment
            );

    return paymentMapper.toResponse(
            savedPayment
    );
}

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll(NEWEST_FIRST)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        return paymentMapper.toResponse(
                findPaymentById(paymentId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByShipmentId(
            Long shipmentId
    ) {
        return paymentRepository
                .findByShipmentIdOrderByCreatedAtDesc(
                        shipmentId
                )
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(
            Long userId
    ) {
        validateUserId(userId);

        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
public PaymentResponse updatePaymentStatus(
        Long paymentId,
        UpdatePaymentStatusRequest request
) {
    Payment payment =
            findPaymentById(paymentId);

    PaymentStatus previousStatus =
            payment.getPaymentStatus();

    PaymentStatus requestedStatus =
            request.getPaymentStatus();

    validateStatusTransition(
            previousStatus,
            requestedStatus
    );

    updateTransactionReference(
            payment,
            request.getTransactionReference()
    );

    updateRemarks(
            payment,
            request.getRemarks()
    );

    payment.setPaymentStatus(
            requestedStatus
    );

    updatePaidDate(
            payment,
            requestedStatus
    );

    Payment updatedPayment =
            paymentRepository.save(payment);

    paymentAuditPublisher
            .publishPaymentStatusUpdated(
                    updatedPayment,
                    previousStatus
            );

    return paymentMapper.toResponse(
            updatedPayment
    );
}

    @Override
public PaymentResponse refundPayment(
        Long paymentId,
        RefundPaymentRequest request
) {
    Payment payment =
            findPaymentById(paymentId);

    PaymentStatus previousStatus =
            payment.getPaymentStatus();

    if (previousStatus != PaymentStatus.PAID) {
        throw new InvalidPaymentStateException(
                "Only PAID payments can be refunded"
        );
    }

    payment.setPaymentStatus(
            PaymentStatus.REFUNDED
    );

    appendRefundReason(
            payment,
            request.getReason()
    );

    Payment refundedPayment =
            paymentRepository.save(payment);

    paymentAuditPublisher
            .publishPaymentRefunded(
                    refundedPayment,
                    previousStatus
            );

    return paymentMapper.toResponse(
            refundedPayment
    );
}

    private Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with ID: "
                                        + paymentId
                        )
                );
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidJwtClaimException(
                    "JWT does not contain a valid user ID"
            );
        }
    }

    private void validateStatusTransition(
            PaymentStatus currentStatus,
            PaymentStatus requestedStatus
    ) {
        if (currentStatus == requestedStatus) {
            throw invalidTransition(
                    currentStatus,
                    requestedStatus
            );
        }

        boolean validTransition = switch (currentStatus) {
            case PENDING ->
                    requestedStatus == PaymentStatus.PAID
                            || requestedStatus
                            == PaymentStatus.FAILED
                            || requestedStatus
                            == PaymentStatus.CANCELLED;

            case FAILED ->
                    requestedStatus == PaymentStatus.PENDING;

            case PAID, REFUNDED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw invalidTransition(
                    currentStatus,
                    requestedStatus
            );
        }
    }

    private InvalidPaymentStateException invalidTransition(
            PaymentStatus currentStatus,
            PaymentStatus requestedStatus
    ) {
        return new InvalidPaymentStateException(
                "Payment status cannot be changed from "
                        + currentStatus
                        + " to "
                        + requestedStatus
        );
    }

    private void updateTransactionReference(
            Payment payment,
            String requestedReference
    ) {
        String normalizedReference =
                normalizeNullable(requestedReference);

        if (normalizedReference == null) {
            return;
        }

        validateTransactionReferenceAvailable(
                normalizedReference,
                payment.getId()
        );

        payment.setTransactionReference(
                normalizedReference
        );
    }

    private void validateTransactionReferenceAvailable(
            String transactionReference,
            Long currentPaymentId
    ) {
        if (transactionReference == null) {
            return;
        }

        paymentRepository
                .findByTransactionReference(
                        transactionReference
                )
                .filter(existingPayment ->
                        !Objects.equals(
                                existingPayment.getId(),
                                currentPaymentId
                        )
                )
                .ifPresent(existingPayment -> {
                    throw new DuplicateTransactionReferenceException(
                            "Transaction reference already exists: "
                                    + transactionReference
                    );
                });
    }

    private void updatePaidDate(
            Payment payment,
            PaymentStatus requestedStatus
    ) {
        if (requestedStatus == PaymentStatus.PAID) {
            payment.setPaidDate(LocalDate.now());
            return;
        }

        payment.setPaidDate(null);
    }

    private void updateRemarks(
            Payment payment,
            String requestedRemarks
    ) {
        String normalizedRemarks =
                normalizeNullable(requestedRemarks);

        if (normalizedRemarks != null) {
            payment.setRemarks(normalizedRemarks);
        }
    }

    private void appendRefundReason(
            Payment payment,
            String refundReason
    ) {
        String refundNote =
                "Refunded: " + refundReason.trim();

        String existingRemarks =
                normalizeNullable(payment.getRemarks());

        if (existingRemarks == null) {
            payment.setRemarks(refundNote);
            return;
        }

        payment.setRemarks(
                existingRemarks
                        + " | "
                        + refundNote
        );
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
