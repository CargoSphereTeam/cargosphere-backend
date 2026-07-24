package com.cargosphere.payment.dto;

import com.cargosphere.payment.entity.enums.PaymentMethod;
import com.cargosphere.payment.entity.enums.PaymentType;
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
public class CreatePaymentRequest {

    @NotNull(message = "Shipment ID is required")
    @Positive(message = "Shipment ID must be positive")
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
    private BigDecimal amount;

    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "Currency must contain exactly 3 letters"
    )
    private String currency;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @Size(
            max = 100,
            message = "Transaction reference cannot exceed 100 characters"
    )
    private String transactionReference;

    private LocalDate dueDate;

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    private String remarks;
}