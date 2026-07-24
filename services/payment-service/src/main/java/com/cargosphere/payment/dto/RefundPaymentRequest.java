package com.cargosphere.payment.dto;

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
public class RefundPaymentRequest {

    @NotBlank(message = "Refund reason is required")
    @Size(
            max = 500,
            message = "Refund reason cannot exceed 500 characters"
    )
    private String reason;
}