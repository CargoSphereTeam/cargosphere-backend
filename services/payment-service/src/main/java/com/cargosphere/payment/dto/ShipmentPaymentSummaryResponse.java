package com.cargosphere.payment.dto;

import com.cargosphere.payment.entity.enums.ConfirmationStatus;
import com.cargosphere.payment.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentPaymentSummaryResponse {

    private Long id;

    private Long shipmentId;

    private BigDecimal estimatedAmount;

    private BigDecimal baseAmount;

    private BigDecimal charges;

    private BigDecimal taxes;

    private BigDecimal discount;

    private BigDecimal finalAmount;

    private BigDecimal paidAmount;

    private BigDecimal balanceAmount;

    private String currency;

    private PaymentMethod paymentMethod;

    private ConfirmationStatus confirmationStatus;

    private Long confirmedBy;

    private LocalDateTime confirmedAt;

    private String remarks;

    private boolean paymentConfirmed;

    private LocalDateTime updatedAt;
}