package com.cargosphere.payment.service.impl;


import com.cargosphere.payment.exception.PaymentAlreadyConfirmedException;
import java.time.LocalDateTime;
import com.cargosphere.payment.exception.PaymentConfirmationNotAllowedException;
import com.cargosphere.payment.exception.InvalidPaymentAmountException;
import com.cargosphere.payment.dto.ShipmentPaymentSummaryRequest;
import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import com.cargosphere.payment.entity.ShipmentPaymentSummary;
import com.cargosphere.payment.mapper.ShipmentPaymentSummaryMapper;
import com.cargosphere.payment.repository.ShipmentPaymentSummaryRepository;
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
import com.cargosphere.payment.entity.enums.ConfirmationStatus;
import java.math.BigDecimal;
import com.cargosphere.payment.entity.enums.PaymentSummaryAction;

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

    private final ShipmentPaymentSummaryRepository
            shipmentPaymentSummaryRepository;

    private final ShipmentPaymentSummaryMapper
            shipmentPaymentSummaryMapper;

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

    @Override
    @Transactional(readOnly = true)
    public ShipmentPaymentSummaryResponse getShipmentPaymentSummary(
            Long shipmentId
    ) {

        ShipmentPaymentSummary summary =
                 shipmentPaymentSummaryRepository
                        .findByShipmentId(shipmentId)
                        .orElseThrow(() ->
                                new PaymentNotFoundException(
                                        "Payment summary not found for shipment ID: "
                                                + shipmentId
                                )
                        );

        return shipmentPaymentSummaryMapper.toResponse(
                summary
        );
    }

    @Override
    public ShipmentPaymentSummaryResponse saveShipmentPaymentSummary(
            Long shipmentId,
            ShipmentPaymentSummaryRequest request,
            Long adminId
    ) {

        ShipmentPaymentSummary summary =
                shipmentPaymentSummaryRepository
                        .findByShipmentId(shipmentId)
                        .orElseGet(ShipmentPaymentSummary::new);

        if (summary.getId() == null) {

            summary.setShipmentId(
                    shipmentId
            );

            summary.setConfirmationStatus(
                    ConfirmationStatus.DRAFT
            );
        }

        summary.setEstimatedAmount(
                request.getEstimatedAmount()
        );

        summary.setBaseAmount(
                request.getBaseAmount()
        );

        summary.setCharges(
                request.getCharges()
        );

        summary.setTaxes(
                request.getTaxes()
        );

        summary.setDiscount(
                request.getDiscount()
        );

        summary.setPaidAmount(
                request.getPaidAmount()
        );

        if (request.getCurrency() != null) {
            summary.setCurrency(request.getCurrency());
        }

        if (request.getPaymentMethod() != null) {
            summary.setPaymentMethod(request.getPaymentMethod());
        }

        summary.setRemarks(
                request.getRemarks()
        );

        BigDecimal finalAmount =
                calculateFinalAmount(request);

        BigDecimal balanceAmount =
                calculateBalanceAmount(
                        finalAmount,
                        request
                );

        validateAmounts(
                request,
                finalAmount,
                balanceAmount
        );

        summary.setFinalAmount(
                finalAmount
        );

        summary.setBalanceAmount(
                balanceAmount
        );
        if (request.getAction() == PaymentSummaryAction.SAVE_DRAFT) {

            ShipmentPaymentSummary savedSummary =
                    handleSaveDraft(summary);

            return shipmentPaymentSummaryMapper.toResponse(
                    savedSummary
            );
        }
        if (isAlreadyConfirmed(summary)) {

            if (summary.getConfirmedBy() != null
                    && Objects.equals(
                    summary.getConfirmedBy(),
                    adminId
            )
                    && summary.getFinalAmount().compareTo(finalAmount) == 0
                    && summary.getPaidAmount().compareTo(
                    request.getPaidAmount()
            ) == 0) {

                return shipmentPaymentSummaryMapper.toResponse(
                        summary
                );
            }

            throw new PaymentAlreadyConfirmedException(
                    "Payment summary has already been confirmed."
            );
        }

        validateConfirmationAllowed(
                balanceAmount
        );

        summary.setConfirmationStatus(
                ConfirmationStatus.CONFIRMED
        );

        summary.setConfirmedBy(
                adminId
        );

        summary.setConfirmedAt(
                LocalDateTime.now()
        );

        ShipmentPaymentSummary confirmedSummary =
                shipmentPaymentSummaryRepository.save(
                        summary
                );

        paymentAuditPublisher.publishShipmentPaymentConfirmed(
                confirmedSummary
        );

        return shipmentPaymentSummaryMapper.toResponse(
                confirmedSummary
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

    private BigDecimal calculateFinalAmount(
            ShipmentPaymentSummaryRequest request
    ) {
        return request.getBaseAmount()
                .add(request.getCharges())
                .add(request.getTaxes())
                .subtract(request.getDiscount());
    }

    private BigDecimal calculateBalanceAmount(
            BigDecimal finalAmount,
            ShipmentPaymentSummaryRequest request
    ) {
        return finalAmount.subtract(
                request.getPaidAmount()
        );
    }

    private void validateAmounts(
            ShipmentPaymentSummaryRequest request,
            BigDecimal finalAmount,
            BigDecimal balanceAmount
    ) {

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPaymentAmountException(
                    "Final amount cannot be negative."
            );
        }

        if (request.getPaidAmount().compareTo(finalAmount) > 0) {
            throw new InvalidPaymentAmountException(
                    "Paid amount cannot exceed final amount."
            );
        }

        if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPaymentAmountException(
                    "Balance amount cannot be negative."
            );
        }
    }

    private ShipmentPaymentSummary handleSaveDraft(
            ShipmentPaymentSummary summary
    ) {

        summary.setConfirmationStatus(
                ConfirmationStatus.DRAFT
        );

        summary.setConfirmedBy(null);

        summary.setConfirmedAt(null);

        return shipmentPaymentSummaryRepository.save(
                summary
        );
    }

    private boolean isAlreadyConfirmed(
            ShipmentPaymentSummary summary
    ) {
        return summary.getConfirmationStatus()
                == ConfirmationStatus.CONFIRMED;
    }

    private void validateConfirmationAllowed(
            BigDecimal balanceAmount
    ) {

        if (balanceAmount.compareTo(BigDecimal.ZERO) != 0) {
            throw new PaymentConfirmationNotAllowedException(
                    "Payment cannot be confirmed until the balance amount is zero."
            );
        }
    }


}
