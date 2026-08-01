package com.cargosphere.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
        name = "RefundPaymentRequest",
        description = "Request body used to refund a payment"
)
public class RefundPaymentRequest {

    @NotBlank(message = "Refund reason is required")
    @Size(
            max = 500,
            message = "Refund reason cannot exceed 500 characters"
    )
    @Schema(
            description = "Reason for refunding the payment",
            example = "Shipment was cancelled by the client",
            maxLength = 500,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String reason;
}
