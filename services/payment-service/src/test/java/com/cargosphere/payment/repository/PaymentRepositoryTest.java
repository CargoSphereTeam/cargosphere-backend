package com.cargosphere.payment.repository;

import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
        paymentRepository.flush();
    }

    @Test
    void saveShouldPersistPayment() {
        Payment payment = createPayment(
                1001L,
                101L,
                "TXN-SAVE-1001",
                PaymentStatus.PENDING
        );

        Payment savedPayment =
                paymentRepository.saveAndFlush(payment);

        assertNotNull(savedPayment.getId());
        assertEquals(
                1001L,
                savedPayment.getShipmentId()
        );
        assertEquals(
                101L,
                savedPayment.getUserId()
        );
        assertEquals(
                new BigDecimal("2500.00"),
                savedPayment.getAmount()
        );
        assertEquals(
                "INR",
                savedPayment.getCurrency()
        );
        assertEquals(
                PaymentMethod.UPI,
                savedPayment.getPaymentMethod()
        );
        assertEquals(
                PaymentStatus.PENDING,
                savedPayment.getPaymentStatus()
        );
        assertEquals(
                PaymentType.FULL,
                savedPayment.getPaymentType()
        );
        assertNotNull(savedPayment.getCreatedAt());
        assertNotNull(savedPayment.getUpdatedAt());
    }

    @Test
    void findByShipmentIdShouldReturnMatchingPayments() {
        paymentRepository.save(
                createPayment(
                        1001L,
                        101L,
                        "TXN-SHIPMENT-1",
                        PaymentStatus.PENDING
                )
        );

        paymentRepository.save(
                createPayment(
                        1001L,
                        102L,
                        "TXN-SHIPMENT-2",
                        PaymentStatus.PAID
                )
        );

        paymentRepository.save(
                createPayment(
                        2002L,
                        103L,
                        "TXN-SHIPMENT-3",
                        PaymentStatus.PENDING
                )
        );

        paymentRepository.flush();

        List<Payment> payments =
                paymentRepository
                        .findByShipmentIdOrderByCreatedAtDesc(
                                1001L
                        );

        assertEquals(2, payments.size());

        assertTrue(
                payments.stream()
                        .allMatch(payment ->
                                payment.getShipmentId()
                                        .equals(1001L)
                        )
        );
    }

    @Test
    void findByUserIdShouldReturnMatchingPayments() {
        paymentRepository.save(
                createPayment(
                        1001L,
                        101L,
                        "TXN-USER-1",
                        PaymentStatus.PENDING
                )
        );

        paymentRepository.save(
                createPayment(
                        1002L,
                        101L,
                        "TXN-USER-2",
                        PaymentStatus.PAID
                )
        );

        paymentRepository.save(
                createPayment(
                        1003L,
                        202L,
                        "TXN-USER-3",
                        PaymentStatus.PENDING
                )
        );

        paymentRepository.flush();

        List<Payment> payments =
                paymentRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                101L
                        );

        assertEquals(2, payments.size());

        assertTrue(
                payments.stream()
                        .allMatch(payment ->
                                payment.getUserId()
                                        .equals(101L)
                        )
        );
    }

    @Test
    void findByPaymentStatusShouldReturnMatchingPayments() {
        paymentRepository.save(
                createPayment(
                        1001L,
                        101L,
                        "TXN-STATUS-1",
                        PaymentStatus.PENDING
                )
        );

        paymentRepository.save(
                createPayment(
                        1002L,
                        102L,
                        "TXN-STATUS-2",
                        PaymentStatus.PAID
                )
        );

        paymentRepository.save(
                createPayment(
                        1003L,
                        103L,
                        "TXN-STATUS-3",
                        PaymentStatus.PENDING
                )
        );

        paymentRepository.flush();

        List<Payment> payments =
                paymentRepository
                        .findByPaymentStatusOrderByCreatedAtDesc(
                                PaymentStatus.PENDING
                        );

        assertEquals(2, payments.size());

        assertTrue(
                payments.stream()
                        .allMatch(payment ->
                                payment.getPaymentStatus()
                                        == PaymentStatus.PENDING
                        )
        );
    }

    @Test
    void findByTransactionReferenceShouldReturnPayment() {
        Payment payment = createPayment(
                1001L,
                101L,
                "TXN-FIND-1001",
                PaymentStatus.PAID
        );

        paymentRepository.saveAndFlush(payment);

        Optional<Payment> result =
                paymentRepository
                        .findByTransactionReference(
                                "TXN-FIND-1001"
                        );

        assertTrue(result.isPresent());

        assertEquals(
                "TXN-FIND-1001",
                result.get()
                        .getTransactionReference()
        );

        assertTrue(
                paymentRepository
                        .existsByTransactionReference(
                                "TXN-FIND-1001"
                        )
        );

        assertFalse(
                paymentRepository
                        .existsByTransactionReference(
                                "TXN-NOT-FOUND"
                        )
        );
    }

    @Test
    void duplicateTransactionReferenceShouldBeRejected() {
        Payment firstPayment = createPayment(
                1001L,
                101L,
                "TXN-DUPLICATE",
                PaymentStatus.PENDING
        );

        Payment secondPayment = createPayment(
                2002L,
                202L,
                "TXN-DUPLICATE",
                PaymentStatus.PENDING
        );

        paymentRepository.saveAndFlush(firstPayment);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository
                        .saveAndFlush(secondPayment)
        );
    }

    private Payment createPayment(
            Long shipmentId,
            Long userId,
            String transactionReference,
            PaymentStatus paymentStatus
    ) {
        Payment payment = Payment.builder()
                .shipmentId(shipmentId)
                .userId(userId)
                .amount(new BigDecimal("2500.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(paymentStatus)
                .paymentType(PaymentType.FULL)
                .transactionReference(
                        transactionReference
                )
                .dueDate(
                        LocalDate.of(
                                2026,
                                8,
                                31
                        )
                )
                .remarks("Repository integration test")
                .build();

        if (paymentStatus == PaymentStatus.PAID) {
            payment.setPaidDate(LocalDate.now());
        }

        return payment;
    }
}