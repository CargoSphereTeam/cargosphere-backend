package com.cargosphere.payment.dto;

import com.cargosphere.payment.entity.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    @Size(
            max = 100,
            message = "Transaction reference cannot exceed 100 characters"
    )
    private String transactionReference;

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    private String remarks;
}