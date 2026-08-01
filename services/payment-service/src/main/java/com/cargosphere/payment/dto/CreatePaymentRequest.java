package com.cargosphere.payment.dto;

import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "CreatePaymentRequest",
        description = "Request body used to create a payment"
)
public class CreatePaymentRequest {

    @NotNull(message = "Shipment ID is required")
    @Positive(message = "Shipment ID must be positive")
    @Schema(
            description = "Shipment database ID",
            example = "1001",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long shipmentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    @Digits(
            integer = 13,
            fraction = 2,
            message = "Amount can contain up to 13 integer digits and 2 decimal places"
    )
    @Schema(
            description = "Payment amount",
            example = "12500.00",
            minimum = "0.01",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal amount;

    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "Currency must contain exactly 3 letters"
    )
    @Schema(
            description = "ISO-style three-letter currency code",
            example = "INR",
            pattern = "^[A-Za-z]{3}$"
    )
    private String currency;

    @NotNull(message = "Payment method is required")
    @Schema(
            description = "Payment method",
            example = "UPI",
            allowableValues = {
                    "UPI",
                    "BANK_TRANSFER",
                    "CREDIT_CARD",
                    "DEBIT_CARD",
                    "CASH",
                    "OTHER"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment type is required")
    @Schema(
            description = "Payment type",
            example = "FULL",
            allowableValues = {
                    "FULL",
                    "PARTIAL",
                    "ADVANCE",
                    "BALANCE"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PaymentType paymentType;

    @Size(
            max = 100,
            message = "Transaction reference cannot exceed 100 characters"
    )
    @Schema(
            description = "Optional external transaction reference",
            example = "TXN-2026-000123",
            maxLength = 100
    )
    private String transactionReference;

    @Schema(
            description = "Payment due date",
            example = "2026-08-15",
            type = "string",
            format = "date"
    )
    private LocalDate dueDate;

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    @Schema(
            description = "Optional payment remarks",
            example = "Advance payment for shipment booking",
            maxLength = 500
    )
    private String remarks;
}
