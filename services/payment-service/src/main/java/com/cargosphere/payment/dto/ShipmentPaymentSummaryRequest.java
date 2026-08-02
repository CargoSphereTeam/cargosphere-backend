package com.cargosphere.payment.dto;

import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentSummaryAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ShipmentPaymentSummaryRequest",
        description = "Request body used to save or confirm a shipment payment summary"
)
public class ShipmentPaymentSummaryRequest {

    private BigDecimal estimatedAmount;

    private BigDecimal baseAmount;

    private BigDecimal charges;

    private BigDecimal taxes;

    private BigDecimal discount;

    private BigDecimal paidAmount;

    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "Currency must contain exactly 3 letters"
    )
    private String currency;

    private PaymentMethod paymentMethod;

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    private String remarks;

    @NotNull(message = "Action is required")
    private PaymentSummaryAction action;
}