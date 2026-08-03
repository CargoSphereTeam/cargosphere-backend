package com.cargosphere.payment.service.impl;

import com.cargosphere.payment.audit.PaymentAuditPublisher;
import com.cargosphere.payment.dto.CreatePaymentRequest;
import com.cargosphere.payment.dto.PaymentResponse;
import com.cargosphere.payment.dto.RefundPaymentRequest;
import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import com.cargosphere.payment.dto.UpdatePaymentStatusRequest;
import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.ShipmentPaymentSummary;
import com.cargosphere.payment.entity.enums.ConfirmationStatus;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import com.cargosphere.payment.exception.DuplicateTransactionReferenceException;
import com.cargosphere.payment.exception.InvalidJwtClaimException;
import com.cargosphere.payment.exception.InvalidPaymentStateException;
import com.cargosphere.payment.exception.PaymentNotFoundException;
import com.cargosphere.payment.mapper.PaymentMapper;
import com.cargosphere.payment.mapper.ShipmentPaymentSummaryMapper;
import com.cargosphere.payment.repository.PaymentRepository;
import com.cargosphere.payment.repository.ShipmentPaymentSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private ShipmentPaymentSummaryRepository shipmentPaymentSummaryRepository;

    @Mock
    private ShipmentPaymentSummaryMapper shipmentPaymentSummaryMapper;

    @Mock
    private PaymentAuditPublisher paymentAuditPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPaymentShouldSavePaymentWhenRequestIsValid() {
        CreatePaymentRequest request =
                createRequest(" TXN-1001 ");

        Payment payment = pendingPayment();

        PaymentResponse expectedResponse =
                response(PaymentStatus.PENDING);

        when(paymentRepository
                .findByTransactionReference("TXN-1001"))
                .thenReturn(Optional.empty());

        when(paymentMapper.toEntity(request, 10L))
                .thenReturn(payment);

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(expectedResponse);

        PaymentResponse actualResponse =
                paymentService.createPayment(
                        request,
                        10L
                );

        assertSame(expectedResponse, actualResponse);

        verify(paymentRepository)
                .findByTransactionReference("TXN-1001");

        verify(paymentMapper)
                .toEntity(request, 10L);

        verify(paymentRepository).save(payment);
    }

    @Test
    void createPaymentShouldThrowWhenUserIdIsInvalid() {
        CreatePaymentRequest request =
                createRequest("TXN-1001");

        InvalidJwtClaimException exception =
                assertThrows(
                        InvalidJwtClaimException.class,
                        () -> paymentService.createPayment(
                                request,
                                0L
                        )
                );

        assertEquals(
                "JWT does not contain a valid user ID",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentRepository,
                paymentMapper
        );
    }

    @Test
    void createPaymentShouldThrowWhenReferenceAlreadyExists() {
        CreatePaymentRequest request =
                createRequest("TXN-1001");

        Payment existingPayment =
                pendingPayment();

        existingPayment.setId(99L);

        when(paymentRepository
                .findByTransactionReference("TXN-1001"))
                .thenReturn(
                        Optional.of(existingPayment)
                );

        DuplicateTransactionReferenceException exception =
                assertThrows(
                        DuplicateTransactionReferenceException.class,
                        () -> paymentService.createPayment(
                                request,
                                10L
                        )
                );

        assertEquals(
                "Transaction reference already exists: TXN-1001",
                exception.getMessage()
        );

        verify(paymentMapper, never())
                .toEntity(any(), any());
    }

    @Test
    void getPaymentByIdShouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findById(99L))
                .thenReturn(Optional.empty());

        PaymentNotFoundException exception =
                assertThrows(
                        PaymentNotFoundException.class,
                        () -> paymentService
                                .getPaymentById(99L)
                );

        assertEquals(
                "Payment not found with ID: 99",
                exception.getMessage()
        );
    }

    @Test
    void updatePaymentStatusShouldMarkPaymentAsPaid() {
        Payment payment = pendingPayment();

        UpdatePaymentStatusRequest request =
                UpdatePaymentStatusRequest.builder()
                        .paymentStatus(PaymentStatus.PAID)
                        .remarks("Payment completed")
                        .build();

        PaymentResponse expectedResponse =
                response(PaymentStatus.PAID);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(expectedResponse);

        PaymentResponse actualResponse =
                paymentService.updatePaymentStatus(
                        1L,
                        request
                );

        assertSame(expectedResponse, actualResponse);
        assertEquals(
                PaymentStatus.PAID,
                payment.getPaymentStatus()
        );
        assertNotNull(payment.getPaidDate());
        assertEquals(
                LocalDate.now(),
                payment.getPaidDate()
        );
        assertEquals(
                "Payment completed",
                payment.getRemarks()
        );

        verify(paymentRepository).save(payment);
    }

    @Test
    void updatePaymentStatusShouldRejectInvalidTransition() {
        Payment payment = pendingPayment();
        payment.setPaymentStatus(PaymentStatus.PAID);

        UpdatePaymentStatusRequest request =
                UpdatePaymentStatusRequest.builder()
                        .paymentStatus(
                                PaymentStatus.PENDING
                        )
                        .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        InvalidPaymentStateException exception =
                assertThrows(
                        InvalidPaymentStateException.class,
                        () -> paymentService
                                .updatePaymentStatus(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "Payment status cannot be changed from PAID to PENDING",
                exception.getMessage()
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    void refundPaymentShouldRefundPaidPayment() {
        Payment payment = pendingPayment();

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidDate(LocalDate.now());
        payment.setRemarks("Payment completed");

        RefundPaymentRequest request =
                RefundPaymentRequest.builder()
                        .reason("Customer requested refund")
                        .build();

        PaymentResponse expectedResponse =
                response(PaymentStatus.REFUNDED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        when(paymentMapper.toResponse(payment))
                .thenReturn(expectedResponse);

        PaymentResponse actualResponse =
                paymentService.refundPayment(
                        1L,
                        request
                );

        assertSame(expectedResponse, actualResponse);
        assertEquals(
                PaymentStatus.REFUNDED,
                payment.getPaymentStatus()
        );
        assertTrue(
                payment.getRemarks().contains(
                        "Refunded: Customer requested refund"
                )
        );

        verify(paymentRepository).save(payment);
    }

    @Test
    void refundPaymentShouldRejectPendingPayment() {
        Payment payment = pendingPayment();

        RefundPaymentRequest request =
                RefundPaymentRequest.builder()
                        .reason("Customer requested refund")
                        .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        InvalidPaymentStateException exception =
                assertThrows(
                        InvalidPaymentStateException.class,
                        () -> paymentService
                                .refundPayment(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "Only PAID payments can be refunded",
                exception.getMessage()
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }


    @Test
    void getShipmentPaymentSummaryShouldReturnSummary() {

        ShipmentPaymentSummary summary =
                ShipmentPaymentSummary.builder()
                        .shipmentId(1001L)
                        .estimatedAmount(new BigDecimal("2500.00"))
                        .confirmationStatus(
                                ConfirmationStatus.DRAFT
                        )
                        .build();

        ShipmentPaymentSummaryResponse response =
                ShipmentPaymentSummaryResponse.builder()
                        .shipmentId(1001L)
                        .estimatedAmount(new BigDecimal("2500.00"))
                        .confirmationStatus(
                                ConfirmationStatus.DRAFT
                        )
                        .build();

        when(shipmentPaymentSummaryRepository
                .findByShipmentId(1001L))
                .thenReturn(Optional.of(summary));

        when(shipmentPaymentSummaryMapper
                .toResponse(summary))
                .thenReturn(response);

        ShipmentPaymentSummaryResponse actual =
                paymentService.getShipmentPaymentSummary(1001L);

        assertSame(response, actual);

        verify(shipmentPaymentSummaryRepository)
                .findByShipmentId(1001L);

        verify(shipmentPaymentSummaryMapper)
                .toResponse(summary);
    }


    @Test
    void getAllPaymentsShouldReturnAllPayments() {

        Payment payment = pendingPayment();

        PaymentResponse response =
                response(PaymentStatus.PENDING);

        Sort sort = Sort.by(
                Sort.Direction.DESC,
                "createdAt"
        );

        when(paymentRepository.findAll(sort))
                .thenReturn(java.util.List.of(payment));

        when(paymentMapper.toResponse(payment))
                .thenReturn(response);

        var actual =
                paymentService.getAllPayments();

        assertEquals(1, actual.size());
        assertSame(response, actual.get(0));

        verify(paymentRepository).findAll(sort);
        verify(paymentMapper).toResponse(payment);
    }


    private CreatePaymentRequest createRequest(
            String transactionReference
    ) {
        return CreatePaymentRequest.builder()
                .shipmentId(1001L)
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .paymentType(PaymentType.FULL)
                .transactionReference(
                        transactionReference
                )
                .build();
    }

    private Payment pendingPayment() {
        return Payment.builder()
                .id(1L)
                .shipmentId(1001L)
                .userId(10L)
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentType(PaymentType.FULL)
                .transactionReference("TXN-1001")
                .remarks("Payment initiated")
                .build();
    }

    private PaymentResponse response(
            PaymentStatus paymentStatus
    ) {
        return PaymentResponse.builder()
                .id(1L)
                .shipmentId(1001L)
                .userId(10L)
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(paymentStatus)
                .paymentType(PaymentType.FULL)
                .transactionReference("TXN-1001")
                .build();
    }
}