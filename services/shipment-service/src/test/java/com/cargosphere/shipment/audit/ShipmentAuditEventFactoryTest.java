package com.cargosphere.shipment.audit;

import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentAuditEventFactoryTest {

    private ShipmentAuditEventFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ShipmentAuditEventFactory();
    }

    @Test
    void shipmentCreatedShouldCreateSuccessEvent() {
        Shipment shipment = shipment();

        AuditEventRequest event =
                factory.shipmentCreated(
                        shipment,
                        new CurrentActor(
                                10L,
                                "ROLE_CLIENT"
                        )
                );

        assertEquals(
                "SHIPMENT_CREATED",
                event.action()
        );

        assertEquals(
                "SHIPMENT",
                event.entityType()
        );

        assertEquals(
                "55",
                event.entityId()
        );

        assertEquals(
                "shipment-service",
                event.serviceName()
        );

        assertEquals(
                "SUCCESS",
                event.outcome()
        );

        assertEquals(
                "POST",
                event.httpMethod()
        );

        assertEquals(
                "/api/shipments",
                event.endpoint()
        );

        assertEquals(
                201,
                event.statusCode()
        );
    }

    @Test
    void statusUpdatedShouldIncludeOldAndNewStatus() {
        Shipment shipment = shipment();
        shipment.setStatus(ShipmentStatus.BOOKED);

        AuditEventRequest event =
                factory.shipmentStatusUpdated(
                        shipment,
                        ShipmentStatus.CREATED,
                        new CurrentActor(
                                1L,
                                "ROLE_ADMIN"
                        )
                );

        assertEquals(
                "SHIPMENT_STATUS_UPDATED",
                event.action()
        );

        assertEquals(
                1L,
                event.actorUserId()
        );

        assertEquals(
                "ROLE_ADMIN",
                event.actorRole()
        );

        assertEquals(
                "Shipment status changed from CREATED to BOOKED",
                event.description()
        );

        assertEquals(
                "PATCH",
                event.httpMethod()
        );

        assertEquals(
                "/api/shipments/55/status",
                event.endpoint()
        );

        assertEquals(
                200,
                event.statusCode()
        );
    }

    private Shipment shipment() {
        return Shipment.builder()
                .id(55L)
                .shipmentNumber(
                        "CS-20260726-ABC12345"
                )
                .clientUserId(10L)
                .status(ShipmentStatus.CREATED)
                .build();
    }
}