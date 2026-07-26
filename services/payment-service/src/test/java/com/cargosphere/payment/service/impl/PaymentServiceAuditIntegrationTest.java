package com.cargosphere.payment.service.impl;

import com.cargosphere.payment.audit.PaymentAuditPublisher;
import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;
import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import com.cargosphere.payment.exception.InvalidPaymentStateException;
import com.cargosphere.payment.mapper.PaymentMapper;
import com.cargosphere.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceAuditIntegrationTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentAuditPublisher
            paymentAuditPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPaymentShouldPublishCreatedEvent() {
        CreatePaymentRequest request =
                createRequest();

        Payment payment =
                payment(PaymentStatus.PENDING);

        PaymentResponse response =
                response(PaymentStatus.PENDING);

        when(paymentRepository
                .findByTransactionReference(
                        anyString()
                ))
                .thenReturn(Optional.empty());

        when(paymentMapper.toEntity(
                request,
                10L
        )).thenReturn(payment);

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        paymentService.createPayment(
                request,
                10L
        );

        verify(paymentAuditPublisher)
                .publishPaymentCreated(payment);
    }

    @Test
    void paidStatusShouldPublishPreviousStatus() {
        Payment payment =
                payment(PaymentStatus.PENDING);

        UpdatePaymentStatusRequest request =
                UpdatePaymentStatusRequest
                        .builder()
                        .paymentStatus(
                                PaymentStatus.PAID
                        )
                        .build();

        when(paymentRepository.findById(55L))
                .thenReturn(
                        Optional.of(payment)
                );

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(
                        response(
                                PaymentStatus.PAID
                        )
                );

        paymentService.updatePaymentStatus(
                55L,
                request
        );

        verify(paymentAuditPublisher)
                .publishPaymentStatusUpdated(
                        payment,
                        PaymentStatus.PENDING
                );
    }

    @Test
    void failedStatusShouldPublishGenericStatusEvent() {
        Payment payment =
                payment(PaymentStatus.PENDING);

        UpdatePaymentStatusRequest request =
                UpdatePaymentStatusRequest
                        .builder()
                        .paymentStatus(
                                PaymentStatus.FAILED
                        )
                        .build();

        when(paymentRepository.findById(55L))
                .thenReturn(
                        Optional.of(payment)
                );

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(
                        response(
                                PaymentStatus.FAILED
                        )
                );

        paymentService.updatePaymentStatus(
                55L,
                request
        );

        verify(paymentAuditPublisher)
                .publishPaymentStatusUpdated(
                        payment,
                        PaymentStatus.PENDING
                );
    }

    @Test
    void refundShouldPublishPreviousPaidStatus() {
        Payment payment =
                payment(PaymentStatus.PAID);

        RefundPaymentRequest request =
                RefundPaymentRequest
                        .builder()
                        .reason(
                                "Customer requested refund"
                        )
                        .build();

        when(paymentRepository.findById(55L))
                .thenReturn(
                        Optional.of(payment)
                );

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(
                        response(
                                PaymentStatus.REFUNDED
                        )
                );

        paymentService.refundPayment(
                55L,
                request
        );

        verify(paymentAuditPublisher)
                .publishPaymentRefunded(
                        payment,
                        PaymentStatus.PAID
                );
    }

    @Test
    void invalidTransitionShouldNotPublishSuccessEvent() {
        Payment payment =
                payment(PaymentStatus.PAID);

        UpdatePaymentStatusRequest request =
                UpdatePaymentStatusRequest
                        .builder()
                        .paymentStatus(
                                PaymentStatus.PENDING
                        )
                        .build();

        when(paymentRepository.findById(55L))
                .thenReturn(
                        Optional.of(payment)
                );

        assertThrows(
                InvalidPaymentStateException.class,
                () -> paymentService
                        .updatePaymentStatus(
                                55L,
                                request
                        )
        );

        verify(
                paymentAuditPublisher,
                never()
        ).publishPaymentStatusUpdated(
                payment,
                PaymentStatus.PAID
        );
    }

    private CreatePaymentRequest createRequest() {
        return CreatePaymentRequest
                .builder()
                .shipmentId(1001L)
                .amount(
                        new BigDecimal("2500.00")
                )
                .currency("INR")
                .paymentMethod(
                        PaymentMethod.UPI
                )
                .paymentType(
                        PaymentType.FULL
                )
                .transactionReference(
                        "TXN-AUDIT-1001"
                )
                .build();
    }

    private Payment payment(
            PaymentStatus status
    ) {
        return Payment.builder()
                .id(55L)
                .shipmentId(1001L)
                .userId(10L)
                .amount(
                        new BigDecimal("2500.00")
                )
                .currency("INR")
                .paymentMethod(
                        PaymentMethod.UPI
                )
                .paymentStatus(status)
                .paymentType(
                        PaymentType.FULL
                )
                .transactionReference(
                        "TXN-AUDIT-1001"
                )
                .build();
    }

    private PaymentResponse response(
            PaymentStatus status
    ) {
        return PaymentResponse.builder()
                .id(55L)
                .shipmentId(1001L)
                .userId(10L)
                .paymentStatus(status)
                .build();
    }
}