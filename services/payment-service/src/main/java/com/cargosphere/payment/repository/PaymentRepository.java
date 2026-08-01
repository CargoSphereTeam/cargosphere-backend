package com.cargosphere.payment.repository;

import com.cargosphere.payment.entity.Payment;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    List<Payment> findByShipmentIdOrderByCreatedAtDesc(
            Long shipmentId
    );

    List<Payment> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Payment> findByPaymentStatusOrderByCreatedAtDesc(
            PaymentStatus paymentStatus
    );

    Optional<Payment> findByTransactionReference(
            String transactionReference
    );

    boolean existsByTransactionReference(
            String transactionReference
    );
}