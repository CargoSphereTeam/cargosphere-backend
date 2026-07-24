package com.cargosphere.payment.entity;

import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentStatus;
import com.cargosphere.payment.entity.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        schema = "payment_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_transaction_reference",
                        columnNames = "transaction_reference"
                )
        },
        indexes = {
                @Index(
                        name = "idx_payments_shipment_id",
                        columnList = "shipment_id"
                ),
                @Index(
                        name = "idx_payments_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_payments_status",
                        columnList = "payment_status"
                ),
                @Index(
                        name = "idx_payments_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "shipment_id",
            nullable = false
    )
    private Long shipmentId;

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    @Column(
            name = "amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Builder.Default
    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 30
    )
    private PaymentMethod paymentMethod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 20
    )
    private PaymentStatus paymentStatus =
            PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_type",
            nullable = false,
            length = 20
    )
    private PaymentType paymentType;

    @Column(
            name = "transaction_reference",
            unique = true,
            length = 100
    )
    private String transactionReference;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(
            name = "remarks",
            length = 500
    )
    private String remarks;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }

        if (currency == null || currency.isBlank()) {
            currency = "INR";
        } else {
            currency = currency.trim().toUpperCase();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();

        if (currency != null) {
            currency = currency.trim().toUpperCase();
        }
    }
}