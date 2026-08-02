package com.cargosphere.payment.mapper;

import com.cargosphere.payment.dto.ShipmentPaymentSummaryRequest;
import com.cargosphere.payment.dto.ShipmentPaymentSummaryResponse;
import com.cargosphere.payment.entity.ShipmentPaymentSummary;
import org.springframework.stereotype.Component;

@Component
public class ShipmentPaymentSummaryMapper {

    public ShipmentPaymentSummary toEntity(
            ShipmentPaymentSummaryRequest request,
            Long shipmentId
    ) {
        return ShipmentPaymentSummary.builder()
                .shipmentId(shipmentId)
                .estimatedAmount(request.getEstimatedAmount())
                .baseAmount(request.getBaseAmount())
                .charges(request.getCharges())
                .taxes(request.getTaxes())
                .discount(request.getDiscount())
                .paidAmount(request.getPaidAmount())
                .currency(normalizeCurrency(
                        request.getCurrency()
                ))
                .paymentMethod(request.getPaymentMethod())
                .remarks(
                        normalizeNullable(
                                request.getRemarks()
                        )
                )
                .build();
    }

    public ShipmentPaymentSummaryResponse toResponse(
            ShipmentPaymentSummary summary
    ) {
        return ShipmentPaymentSummaryResponse.builder()
                .id(summary.getId())
                .shipmentId(summary.getShipmentId())
                .estimatedAmount(summary.getEstimatedAmount())
                .baseAmount(summary.getBaseAmount())
                .charges(summary.getCharges())
                .taxes(summary.getTaxes())
                .discount(summary.getDiscount())
                .finalAmount(summary.getFinalAmount())
                .paidAmount(summary.getPaidAmount())
                .balanceAmount(summary.getBalanceAmount())
                .currency(summary.getCurrency())
                .paymentMethod(summary.getPaymentMethod())
                .confirmationStatus(summary.getConfirmationStatus())
                .confirmedBy(summary.getConfirmedBy())
                .confirmedAt(summary.getConfirmedAt())
                .remarks(summary.getRemarks())
                .paymentConfirmed(
                        summary.getConfirmationStatus() != null
                                && summary.getConfirmationStatus().name().equals("CONFIRMED")
                )
                .updatedAt(summary.getUpdatedAt())
                .build();
    }

    private String normalizeCurrency(String currency) {

        if (currency == null || currency.isBlank()) {
            return "INR";
        }

        return currency.trim().toUpperCase();
    }

    private String normalizeNullable(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}