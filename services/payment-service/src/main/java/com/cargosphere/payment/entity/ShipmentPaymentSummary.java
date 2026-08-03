package com.cargosphere.payment.entity;

import com.cargosphere.payment.entity.enums.ConfirmationStatus;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipment_payment_summaries",
        schema = "payment_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shipment_payment_summary",
                        columnNames = "shipment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_payment_summary_confirmation_status",
                        columnList = "confirmation_status"
                ),
                @Index(
                        name = "idx_payment_summary_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentPaymentSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "shipment_id",
            nullable = false
    )
    private Long shipmentId;

    @Column(
            name = "estimated_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal estimatedAmount;

    @Column(
            name = "base_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal baseAmount;

    @Column(
            name = "charges",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal charges;

    @Column(
            name = "taxes",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal taxes;

    @Column(
            name = "discount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal discount;

    @Column(
            name = "final_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal finalAmount;

    @Column(
            name = "paid_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal paidAmount;

    @Column(
            name = "balance_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal balanceAmount;

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
            length = 30
    )
    private PaymentMethod paymentMethod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "confirmation_status",
            nullable = false,
            length = 30
    )
    private ConfirmationStatus confirmationStatus =
            ConfirmationStatus.DRAFT;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

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

        if (confirmationStatus == null) {
            confirmationStatus = ConfirmationStatus.DRAFT;
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

        if (currency != null && !currency.isBlank()) {
            currency = currency.trim().toUpperCase();
        }
    }
}