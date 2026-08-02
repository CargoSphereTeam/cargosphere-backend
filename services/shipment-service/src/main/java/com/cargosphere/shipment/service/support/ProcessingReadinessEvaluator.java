package com.cargosphere.shipment.service.support;

import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.integration.container.ContainerAllocationResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentResponse;
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
            List<ShipmentDocumentResponse> documents,
            List<ShipmentPaymentResponse> payments
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
                        documents
                );

        boolean paymentReady =
                isPaymentReady(
                        shipmentId,
                        payments
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
                        processingStageReady
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
        return allocations.stream()
                .anyMatch(allocation ->
                        allocation != null
                                && allocation.allocationId()
                                != null
                                && Objects.equals(
                                allocation.shipmentId(),
                                shipmentId
                        )
                                && allocation.containerTypeId()
                                != null
                                && allocation.quantity()
                                != null
                                && allocation.quantity() >= 1
                );
    }

    public boolean isCargoReady(
            List<CargoDetail> cargoDetails,
            List<CargoVerification> verifications
    ) {
        if (cargoDetails.isEmpty()) {
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
            List<ShipmentDocumentResponse> documents
    ) {
        if (documents.isEmpty()) {
            return false;
        }

        return documents.stream()
                .allMatch(document -> {
                    if (document == null
                            || document.id() == null
                            || !Objects.equals(
                            document.shipmentId(),
                            shipmentId
                    )) {
                        return false;
                    }

                    if (!Boolean.TRUE.equals(
                            document.required()
                    )) {
                        return true;
                    }

                    return "VERIFIED".equalsIgnoreCase(
                            document.verificationStatus()
                    )
                            && document.verifiedBy() != null
                            && document.verifiedAt() != null;
                });
    }

    public boolean isPaymentReady(
            Long shipmentId,
            List<ShipmentPaymentResponse> payments
    ) {
        return payments.stream()
                .anyMatch(payment ->
                        payment != null
                                && payment.id() != null
                                && Objects.equals(
                                payment.shipmentId(),
                                shipmentId
                        )
                                && payment.amount() != null
                                && payment.amount()
                                .compareTo(BigDecimal.ZERO) > 0
                                && "PAID".equalsIgnoreCase(
                                payment.paymentStatus()
                        )
                                && payment.paidDate() != null
                );
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
                                && verification.getVerifiedBy()
                                != null
                                && verification.getVerifiedAt()
                                != null
                );
    }

    private List<String> buildBlockingReasons(
            boolean containerReady,
            boolean cargoReady,
            boolean documentsReady,
            boolean paymentReady,
            boolean processingStageReady
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
            reasons.add(
                    "All required shipment documents must be verified"
            );
        }

        if (!paymentReady) {
            reasons.add(
                    "At least one valid PAID payment is required"
            );
        }

        if (!processingStageReady) {
            reasons.add(
                    "Processing stage must be READY_FOR_EBILL"
            );
        }

        return List.copyOf(reasons);
    }
}
