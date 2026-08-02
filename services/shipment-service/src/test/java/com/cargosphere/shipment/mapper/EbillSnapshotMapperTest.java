package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillSnapshot;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import com.cargosphere.shipment.integration.auth.AuthUserResponse;
import com.cargosphere.shipment.integration.container.ContainerAllocationResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EbillSnapshotMapperTest {

    private final EbillSnapshotMapper mapper =
            new EbillSnapshotMapper();

    @Test
    void shouldMapCompleteImmutableEbillSnapshot() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        1,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        Shipment shipment = Shipment.builder()
                .id(10L)
                .shipmentNumber("CS-20260803-ABC12345")
                .clientUserId(100L)
                .originLocation("Mumbai")
                .destinationLocation("Delhi")
                .shipmentType(ShipmentType.ROAD)
                .status(ShipmentStatus.CREATED)
                .expectedPickupDate(
                        LocalDate.of(2026, 8, 5)
                )
                .expectedDeliveryDate(
                        LocalDate.of(2026, 8, 8)
                )
                .processingStage(
                        ProcessingStage.READY_FOR_EBILL
                )
                .processingStartedAt(
                        generatedAt.minusDays(2)
                )
                .createdAt(
                        generatedAt.minusDays(3)
                )
                .updatedAt(
                        generatedAt.minusHours(1)
                )
                .build();

        LocalDateTime cargoTime =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        10,
                        0
                );

        CargoDetail cargoDetail = CargoDetail.builder()
                .id(20L)
                .shipment(shipment)
                .cargoName("Electronics")
                .cargoDescription("Laptop shipment")
                .cargoType(CargoType.GENERAL)
                .weightKg(new BigDecimal("50.00"))
                .volumeCbm(new BigDecimal("2.50"))
                .quantity(5)
                .fragile(true)
                .hazardous(false)
                .createdAt(cargoTime)
                .updatedAt(cargoTime.plusHours(1))
                .build();

        CargoVerification verification =
                CargoVerification.builder()
                        .id(30L)
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.CONFIRMED
                        )
                        .confirmedCargoName(
                                "Verified Electronics"
                        )
                        .confirmedCargoDescription(
                                "Verified laptop shipment"
                        )
                        .confirmedCargoType(
                                CargoType.GENERAL
                        )
                        .confirmedWeightKg(
                                new BigDecimal("49.50")
                        )
                        .confirmedVolumeCbm(
                                new BigDecimal("2.40")
                        )
                        .confirmedQuantity(5)
                        .confirmedFragile(true)
                        .confirmedHazardous(false)
                        .verificationRemarks(
                                "Cargo physically verified"
                        )
                        .verifiedBy(5L)
                        .verifiedAt(cargoTime.plusHours(2))
                        .createdAt(cargoTime.plusHours(1))
                        .updatedAt(cargoTime.plusHours(2))
                        .build();

        AuthUserResponse client =
                new AuthUserResponse(
                        100L,
                        "Cargo Client",
                        "client@cargosphere.com",
                        "9876543210",
                        "ROLE_CLIENT",
                        "ACTIVE",
                        cargoTime.minusDays(30),
                        cargoTime.minusDays(1)
                );

        ContainerAllocationResponse allocation =
                new ContainerAllocationResponse(
                        40L,
                        10L,
                        2L,
                        "DRY_20",
                        "20 Foot Dry Container",
                        1,
                        "ALLOCATED",
                        "Primary allocation",
                        cargoTime.plusHours(3),
                        cargoTime.plusHours(3)
                );

        ShipmentDocumentResponse document =
                new ShipmentDocumentResponse(
                        50L,
                        10L,
                        "INVOICE",
                        true,
                        "VERIFIED",
                        5L,
                        cargoTime.plusHours(4),
                        "Verified successfully",
                        cargoTime,
                        cargoTime.plusHours(4)
                );

        ShipmentPaymentResponse payment =
                new ShipmentPaymentResponse(
                        60L,
                        10L,
                        100L,
                        new BigDecimal("25000.00"),
                        "INR",
                        "UPI",
                        "PAID",
                        "FULL",
                        "TXN-10001",
                        LocalDate.of(2026, 8, 2),
                        LocalDate.of(2026, 8, 2),
                        "Full payment received",
                        cargoTime,
                        cargoTime.plusHours(5)
                );

        ShipmentEvent laterEvent =
                ShipmentEvent.builder()
                        .id(72L)
                        .shipment(shipment)
                        .eventType(
                                ShipmentEventType.PAYMENT_CONFIRMED
                        )
                        .eventDescription(
                                "Payment confirmed"
                        )
                        .eventLocation("Delhi")
                        .eventTime(
                                cargoTime.plusHours(6)
                        )
                        .createdAt(
                                cargoTime.plusHours(6)
                        )
                        .updatedAt(
                                cargoTime.plusHours(6)
                        )
                        .build();

        ShipmentEvent earlierEvent =
                ShipmentEvent.builder()
                        .id(71L)
                        .shipment(shipment)
                        .eventType(
                                ShipmentEventType.CREATED
                        )
                        .eventDescription(
                                "Shipment created"
                        )
                        .eventLocation("Mumbai")
                        .eventTime(cargoTime)
                        .createdAt(cargoTime)
                        .updatedAt(cargoTime)
                        .build();

        ProcessingReadinessResponse readiness =
                ProcessingReadinessResponse.builder()
                        .shipmentId(10L)
                        .shipmentNumber(
                                "CS-20260803-ABC12345"
                        )
                        .processingStage(
                                ProcessingStage.READY_FOR_EBILL
                        )
                        .containerReady(true)
                        .cargoReady(true)
                        .documentsReady(true)
                        .paymentReady(true)
                        .ebillReady(true)
                        .blockingReasons(List.of())
                        .build();

        EbillSnapshot snapshot = mapper.toSnapshot(
                "EBL-20260803-12345678",
                1,
                generatedAt,
                5L,
                shipment,
                client,
                List.of(cargoDetail),
                List.of(verification),
                List.of(allocation),
                List.of(document),
                List.of(payment),
                List.of(laterEvent, earlierEvent),
                readiness
        );

        assertThat(snapshot.schemaVersion())
                .isEqualTo("1.0");

        assertThat(snapshot.ebillNumber())
                .isEqualTo("EBL-20260803-12345678");

        assertThat(snapshot.ebillVersion())
                .isEqualTo(1);

        assertThat(snapshot.generatedAt())
                .isEqualTo(generatedAt);

        assertThat(snapshot.generatedBy())
                .isEqualTo(5L);

        assertThat(snapshot.shipment().shipmentId())
                .isEqualTo(10L);

        assertThat(snapshot.client().userId())
                .isEqualTo(100L);

        assertThat(snapshot.originalCargo())
                .singleElement()
                .extracting(
                        original ->
                                original.cargoDetailId()
                )
                .isEqualTo(20L);

        assertThat(snapshot.confirmedCargo())
                .singleElement()
                .extracting(
                        confirmed ->
                                confirmed.verificationId()
                )
                .isEqualTo(30L);

        assertThat(snapshot.containerAllocations())
                .singleElement()
                .extracting(
                        container ->
                                container.allocationId()
                )
                .isEqualTo(40L);

        assertThat(snapshot.documents())
                .singleElement()
                .extracting(
                        storedDocument ->
                                storedDocument.documentId()
                )
                .isEqualTo(50L);

        assertThat(snapshot.payments())
                .singleElement()
                .extracting(
                        storedPayment ->
                                storedPayment.paymentId()
                )
                .isEqualTo(60L);

        assertThat(snapshot.shipmentEvents())
                .extracting(event -> event.eventId())
                .containsExactly(71L, 72L);

        assertThat(snapshot.readiness().ebillReady())
                .isTrue();

        assertThat(snapshot.readiness().blockingReasons())
                .isEmpty();
    }
}
