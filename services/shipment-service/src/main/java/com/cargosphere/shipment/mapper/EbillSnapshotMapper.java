package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.ebill.EbillPreviewResponse;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillClientSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillConfirmedCargoSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillContainerAllocationSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillDocumentSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillEventSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillOriginalCargoSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillPaymentSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillReadinessSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillShipmentSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillSnapshot;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.integration.auth.AuthUserResponse;
import com.cargosphere.shipment.integration.container.ContainerAllocationResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class EbillSnapshotMapper {

    private static final String SCHEMA_VERSION = "1.0";

    public EbillPreviewResponse toPreview(
            Shipment shipment,
            AuthUserResponse client,
            List<CargoDetail> originalCargo,
            List<CargoVerification> confirmedCargo,
            List<ContainerAllocationResponse> allocations,
            List<ShipmentDocumentResponse> documents,
            List<ShipmentPaymentResponse> payments,
            List<ShipmentEvent> shipmentEvents,
            ProcessingReadinessResponse readiness
    ) {
        return EbillPreviewResponse.builder()
                .shipment(
                        toShipmentSnapshot(shipment)
                )
                .client(
                        toClientSnapshot(client)
                )
                .originalCargo(
                        toOriginalCargoSnapshots(originalCargo)
                )
                .confirmedCargo(
                        toConfirmedCargoSnapshots(confirmedCargo)
                )
                .containerAllocations(
                        toAllocationSnapshots(allocations)
                )
                .documents(
                        toDocumentSnapshots(documents)
                )
                .payments(
                        toPaymentSnapshots(payments)
                )
                .shipmentEvents(
                        toEventSnapshots(shipmentEvents)
                )
                .readiness(
                        toReadinessSnapshot(readiness)
                )
                .build();
    }

    public EbillSnapshot toSnapshot(
            String ebillNumber,
            Integer ebillVersion,
            OffsetDateTime generatedAt,
            Long generatedBy,
            Shipment shipment,
            AuthUserResponse client,
            List<CargoDetail> originalCargo,
            List<CargoVerification> confirmedCargo,
            List<ContainerAllocationResponse> allocations,
            List<ShipmentDocumentResponse> documents,
            List<ShipmentPaymentResponse> payments,
            List<ShipmentEvent> shipmentEvents,
            ProcessingReadinessResponse readiness
    ) {
        return new EbillSnapshot(
                SCHEMA_VERSION,
                ebillNumber,
                ebillVersion,
                generatedAt,
                generatedBy,
                toShipmentSnapshot(shipment),
                toClientSnapshot(client),
                toOriginalCargoSnapshots(originalCargo),
                toConfirmedCargoSnapshots(confirmedCargo),
                toAllocationSnapshots(allocations),
                toDocumentSnapshots(documents),
                toPaymentSnapshots(payments),
                toEventSnapshots(shipmentEvents),
                toReadinessSnapshot(readiness)
        );
    }

    private EbillShipmentSnapshot toShipmentSnapshot(
            Shipment shipment
    ) {
        if (shipment == null) {
            return null;
        }

        return new EbillShipmentSnapshot(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getClientUserId(),
                shipment.getOriginLocation(),
                shipment.getDestinationLocation(),
                shipment.getShipmentType(),
                shipment.getStatus(),
                shipment.getExpectedPickupDate(),
                shipment.getExpectedDeliveryDate(),
                shipment.getProcessingStage(),
                shipment.getProcessingStartedAt(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt()
        );
    }

    private EbillClientSnapshot toClientSnapshot(
            AuthUserResponse client
    ) {
        if (client == null) {
            return null;
        }

        return new EbillClientSnapshot(
                client.id(),
                client.fullName(),
                client.email(),
                client.phoneNumber(),
                client.role(),
                client.status(),
                client.createdAt(),
                client.updatedAt()
        );
    }

    private List<EbillOriginalCargoSnapshot>
    toOriginalCargoSnapshots(
            List<CargoDetail> cargoDetails
    ) {
        if (cargoDetails == null) {
            return List.of();
        }

        return cargoDetails.stream()
                .filter(Objects::nonNull)
                .map(this::toOriginalCargoSnapshot)
                .toList();
    }

    private EbillOriginalCargoSnapshot
    toOriginalCargoSnapshot(
            CargoDetail cargoDetail
    ) {
        return new EbillOriginalCargoSnapshot(
                cargoDetail.getId(),
                cargoDetail.getCargoName(),
                cargoDetail.getCargoDescription(),
                cargoDetail.getCargoType(),
                cargoDetail.getWeightKg(),
                cargoDetail.getVolumeCbm(),
                cargoDetail.getQuantity(),
                cargoDetail.getFragile(),
                cargoDetail.getHazardous(),
                cargoDetail.getCreatedAt(),
                cargoDetail.getUpdatedAt()
        );
    }

    private List<EbillConfirmedCargoSnapshot>
    toConfirmedCargoSnapshots(
            List<CargoVerification> verifications
    ) {
        if (verifications == null) {
            return List.of();
        }

        return verifications.stream()
                .filter(Objects::nonNull)
                .map(this::toConfirmedCargoSnapshot)
                .toList();
    }

    private EbillConfirmedCargoSnapshot
    toConfirmedCargoSnapshot(
            CargoVerification verification
    ) {
        Long cargoDetailId =
                verification.getCargoDetail() == null
                        ? null
                        : verification
                        .getCargoDetail()
                        .getId();

        return new EbillConfirmedCargoSnapshot(
                verification.getId(),
                cargoDetailId,
                verification.getVerificationStatus(),
                verification.getConfirmedCargoName(),
                verification.getConfirmedCargoDescription(),
                verification.getConfirmedCargoType(),
                verification.getConfirmedWeightKg(),
                verification.getConfirmedVolumeCbm(),
                verification.getConfirmedQuantity(),
                verification.getConfirmedFragile(),
                verification.getConfirmedHazardous(),
                verification.getVerificationRemarks(),
                verification.getVerifiedBy(),
                verification.getVerifiedAt(),
                verification.getCreatedAt(),
                verification.getUpdatedAt()
        );
    }

    private List<EbillContainerAllocationSnapshot>
    toAllocationSnapshots(
            List<ContainerAllocationResponse> allocations
    ) {
        if (allocations == null) {
            return List.of();
        }

        return allocations.stream()
                .filter(Objects::nonNull)
                .map(allocation ->
                        new EbillContainerAllocationSnapshot(
                                allocation.allocationId(),
                                allocation.shipmentId(),
                                allocation.containerTypeId(),
                                allocation.containerTypeCode(),
                                allocation.containerTypeName(),
                                allocation.quantity(),
                                allocation.allocationStatus(),
                                allocation.notes(),
                                allocation.allocatedAt(),
                                allocation.updatedAt()
                        )
                )
                .toList();
    }

    private List<EbillDocumentSnapshot>
    toDocumentSnapshots(
            List<ShipmentDocumentResponse> documents
    ) {
        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .filter(Objects::nonNull)
                .map(document ->
                        new EbillDocumentSnapshot(
                                document.id(),
                                document.shipmentId(),
                                document.documentType(),
                                document.required(),
                                document.verificationStatus(),
                                document.verifiedBy(),
                                document.verifiedAt(),
                                document.remarks(),
                                document.createdAt(),
                                document.updatedAt()
                        )
                )
                .toList();
    }

    private List<EbillPaymentSnapshot>
    toPaymentSnapshots(
            List<ShipmentPaymentResponse> payments
    ) {
        if (payments == null) {
            return List.of();
        }

        return payments.stream()
                .filter(Objects::nonNull)
                .map(payment ->
                        new EbillPaymentSnapshot(
                                payment.id(),
                                payment.shipmentId(),
                                payment.userId(),
                                payment.amount(),
                                payment.currency(),
                                payment.paymentMethod(),
                                payment.paymentStatus(),
                                payment.paymentType(),
                                payment.transactionReference(),
                                payment.dueDate(),
                                payment.paidDate(),
                                payment.remarks(),
                                payment.createdAt(),
                                payment.updatedAt()
                        )
                )
                .toList();
    }

    private List<EbillEventSnapshot>
    toEventSnapshots(
            List<ShipmentEvent> events
    ) {
        if (events == null) {
            return List.of();
        }

        return events.stream()
                .filter(Objects::nonNull)
                .sorted(
                        Comparator
                                .comparing(
                                        ShipmentEvent::getEventTime,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                                .thenComparing(
                                        ShipmentEvent::getId,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                )
                .map(this::toEventSnapshot)
                .toList();
    }

    private EbillEventSnapshot toEventSnapshot(
            ShipmentEvent event
    ) {
        Long shipmentId =
                event.getShipment() == null
                        ? null
                        : event.getShipment().getId();

        return new EbillEventSnapshot(
                event.getId(),
                shipmentId,
                event.getEventType(),
                event.getEventDescription(),
                event.getEventLocation(),
                event.getEventTime(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    private EbillReadinessSnapshot
    toReadinessSnapshot(
            ProcessingReadinessResponse readiness
    ) {
        if (readiness == null) {
            return null;
        }

        return new EbillReadinessSnapshot(
                readiness.getProcessingStage(),
                readiness.isContainerReady(),
                readiness.isCargoReady(),
                readiness.isDocumentsReady(),
                readiness.isPaymentReady(),
                readiness.isEbillReady(),
                readiness.getBlockingReasons()
        );
    }
}
