package com.cargosphere.shipment.service.support;

import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.integration.container.ContainerAllocationResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentReadinessResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProcessingReadinessEvaluator {

    public ProcessingReadinessResponse evaluate(
            Shipment shipment,
            List<ContainerAllocationResponse> allocations,
            List<CargoDetail> cargoDetails,
            List<CargoVerification> cargoVerifications,
            ShipmentDocumentReadinessResponse documentReadiness,
            ShipmentPaymentSummaryResponse paymentSummary
    ) {
        Long shipmentId = shipment.getId();

        boolean containerReady =
                isContainerReady(
                        shipmentId,
                        allocations
                );

        boolean cargoReady =
                isCargoReady(
                        cargoDetails,
                        cargoVerifications
                );

        boolean documentsReady =
                areDocumentsReady(
                        shipmentId,
                        documentReadiness
                );

        boolean paymentReady =
                isPaymentReady(
                        shipmentId,
                        paymentSummary
                );

        boolean processingStageReady =
                shipment.getProcessingStage()
                        == ProcessingStage.READY_FOR_EBILL;

        boolean ebillReady =
                containerReady
                        && cargoReady
                        && documentsReady
                        && paymentReady
                        && processingStageReady;

        List<String> blockingReasons =
                buildBlockingReasons(
                        containerReady,
                        cargoReady,
                        documentsReady,
                        paymentReady,
                        processingStageReady,
                        documentReadiness,
                        paymentSummary
                );

        return ProcessingReadinessResponse.builder()
                .shipmentId(shipmentId)
                .shipmentNumber(
                        shipment.getShipmentNumber()
                )
                .processingStage(
                        shipment.getProcessingStage()
                )
                .containerReady(containerReady)
                .cargoReady(cargoReady)
                .documentsReady(documentsReady)
                .paymentReady(paymentReady)
                .ebillReady(ebillReady)
                .blockingReasons(blockingReasons)
                .build();
    }

    public boolean isContainerReady(
            Long shipmentId,
            List<ContainerAllocationResponse> allocations
    ) {
        if (allocations == null || allocations.isEmpty()) {
            return false;
        }

        return allocations.stream()
                .anyMatch(allocation ->
                        allocation != null
                                && allocation.allocationId() != null
                                && Objects.equals(
                                allocation.shipmentId(),
                                shipmentId
                        )
                                && allocation.containerTypeId() != null
                                && allocation.quantity() != null
                                && allocation.quantity() >= 1
                );
    }

    public boolean isCargoReady(
            List<CargoDetail> cargoDetails,
            List<CargoVerification> verifications
    ) {
        if (cargoDetails == null || cargoDetails.isEmpty()) {
            return false;
        }

        if (verifications == null) {
            return false;
        }

        return cargoDetails.stream()
                .allMatch(cargoDetail ->
                        hasConfirmedVerification(
                                cargoDetail,
                                verifications
                        )
                );
    }

    public boolean areDocumentsReady(
            Long shipmentId,
            ShipmentDocumentReadinessResponse readiness
    ) {
        return readiness != null
                && Objects.equals(
                readiness.shipmentId(),
                shipmentId
        )
                && readiness.requiredDocuments() > 0
                && readiness.blockingRequiredDocuments() == 0
                && readiness.allMandatoryDocumentsResolved();
    }

    public boolean isPaymentReady(
            Long shipmentId,
            ShipmentPaymentSummaryResponse summary
    ) {
        if (summary == null) {
            return false;
        }

        if (
                !Objects.equals(
                        summary.shipmentId(),
                        shipmentId
                )
        ) {
            return false;
        }

        if (summary.id() == null) {
            return false;
        }

        if (
                !"CONFIRMED".equalsIgnoreCase(
                        summary.confirmationStatus()
                )
        ) {
            return false;
        }

        if (!summary.paymentConfirmed()) {
            return false;
        }

        if (summary.confirmedBy() == null) {
            return false;
        }

        if (summary.confirmedAt() == null) {
            return false;
        }

        if (
                summary.finalAmount() == null
                        || summary.finalAmount()
                        .compareTo(BigDecimal.ZERO) <= 0
        ) {
            return false;
        }

        if (
                summary.paidAmount() == null
                        || summary.paidAmount()
                        .compareTo(summary.finalAmount()) < 0
        ) {
            return false;
        }

        return summary.balanceAmount() != null
                && summary.balanceAmount()
                .compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean hasConfirmedVerification(
            CargoDetail cargoDetail,
            List<CargoVerification> verifications
    ) {
        return verifications.stream()
                .anyMatch(verification ->
                        verification != null
                                && verification
                                .getCargoDetail() != null
                                && Objects.equals(
                                verification
                                        .getCargoDetail()
                                        .getId(),
                                cargoDetail.getId()
                        )
                                && verification
                                .getVerificationStatus()
                                == CargoVerificationStatus.CONFIRMED
                                && verification.getVerifiedBy() != null
                                && verification.getVerifiedAt() != null
                );
    }

    private List<String> buildBlockingReasons(
            boolean containerReady,
            boolean cargoReady,
            boolean documentsReady,
            boolean paymentReady,
            boolean processingStageReady,
            ShipmentDocumentReadinessResponse documentReadiness,
            ShipmentPaymentSummaryResponse paymentSummary
    ) {
        List<String> reasons = new ArrayList<>();

        if (!containerReady) {
            reasons.add(
                    "No valid container allocation exists"
            );
        }

        if (!cargoReady) {
            reasons.add(
                    "All shipment cargo items must be confirmed"
            );
        }

        if (!documentsReady) {
            if (
                    documentReadiness != null
                            && documentReadiness
                            .blockingDocumentTypes() != null
                            && !documentReadiness
                            .blockingDocumentTypes()
                            .isEmpty()
            ) {
                reasons.add(
                        "Required documents are unresolved: "
                                + String.join(
                                ", ",
                                documentReadiness
                                        .blockingDocumentTypes()
                        )
                );
            } else {
                reasons.add(
                        "All required shipment documents must be "
                                + "VERIFIED or NOT_APPLICABLE"
                );
            }
        }

        if (!paymentReady) {
            reasons.add(
                    buildPaymentBlockingReason(
                            paymentSummary
                    )
            );
        }

        if (!processingStageReady) {
            reasons.add(
                    "Shipment processing stage must be READY_FOR_EBILL"
            );
        }

        return List.copyOf(reasons);
    }

    private String buildPaymentBlockingReason(
            ShipmentPaymentSummaryResponse summary
    ) {
        if (summary == null) {
            return "A shipment payment summary is required";
        }

        if (
                !"CONFIRMED".equalsIgnoreCase(
                        summary.confirmationStatus()
                )
                || !summary.paymentConfirmed()
        ) {
            return "Shipment payment summary must be confirmed";
        }

        if (
                summary.balanceAmount() == null
                        || summary.balanceAmount()
                        .compareTo(BigDecimal.ZERO) != 0
        ) {
            return "Shipment payment balance must be zero";
        }

        if (
                summary.paidAmount() == null
                        || summary.finalAmount() == null
                        || summary.paidAmount()
                        .compareTo(summary.finalAmount()) < 0
        ) {
            return "Paid amount must cover the final shipment amount";
        }

        return "Shipment payment confirmation is incomplete";
    }
}