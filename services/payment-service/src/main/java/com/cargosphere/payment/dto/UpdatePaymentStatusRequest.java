package com.cargosphere.payment.dto;

import com.cargosphere.payment.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "UpdatePaymentStatusRequest",
        description = "Request body used to update payment status"
)
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    @Schema(
            description = "New payment status",
            example = "PAID",
            allowableValues = {
                    "PENDING",
                    "PAID",
                    "FAILED",
                    "REFUNDED",
                    "CANCELLED"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private PaymentStatus paymentStatus;

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

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    @Schema(
            description = "Optional status update remarks",
            example = "Payment confirmed by administrator",
            maxLength = 500
    )
    private String remarks;
}
