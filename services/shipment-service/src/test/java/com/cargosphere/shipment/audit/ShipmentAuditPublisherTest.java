package com.cargosphere.shipment.audit;

import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentAuditPublisherTest {

    @Mock
    private AuditClient auditClient;

    @Mock
    private ShipmentAuditEventFactory
            shipmentAuditEventFactory;

    @Mock
    private ShipmentActorProvider
            shipmentActorProvider;

    @InjectMocks
    private ShipmentAuditPublisher
            shipmentAuditPublisher;

    @Test
    void publishShipmentCreatedShouldPublishEvent() {
        Shipment shipment =
                shipment(ShipmentStatus.CREATED);

        CurrentActor actor =
                new CurrentActor(
                        10L,
                        "ROLE_CLIENT"
                );

        AuditEventRequest event =
                event("SHIPMENT_CREATED");

        when(shipmentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(shipmentAuditEventFactory
                .shipmentCreated(
                        shipment,
                        actor
                ))
                .thenReturn(event);

        shipmentAuditPublisher
                .publishShipmentCreated(shipment);

        verify(auditClient)
                .publish(event);
    }

    @Test
    void publishStatusUpdatedShouldUsePreviousStatus() {
        Shipment shipment =
                shipment(ShipmentStatus.BOOKED);

        CurrentActor actor =
                new CurrentActor(
                        1L,
                        "ROLE_ADMIN"
                );

        AuditEventRequest event =
                event(
                        "SHIPMENT_STATUS_UPDATED"
                );

        when(shipmentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(shipmentAuditEventFactory
                .shipmentStatusUpdated(
                        shipment,
                        ShipmentStatus.CREATED,
                        actor
                ))
                .thenReturn(event);

        shipmentAuditPublisher
                .publishShipmentStatusUpdated(
                        shipment,
                        ShipmentStatus.CREATED
                );

        verify(auditClient)
                .publish(event);
    }

    @Test
    void publishEbillGeneratedShouldPublishEvent() {
        Shipment shipment =
                shipment(ShipmentStatus.BOOKED);

        shipment.setEbillNumber(
                "EBL-20260803-A1B2C3D4"
        );

        CurrentActor actor =
                new CurrentActor(
                        1L,
                        "ROLE_ADMIN"
                );

        AuditEventRequest event =
                event("EBILL_GENERATED");

        when(shipmentActorProvider
                .getCurrentActor())
                .thenReturn(actor);

        when(shipmentAuditEventFactory
                .ebillGenerated(
                        shipment,
                        actor
                ))
                .thenReturn(event);

        shipmentAuditPublisher
                .publishEbillGenerated(shipment);

        verify(auditClient)
                .publish(event);
    }

    private Shipment shipment(
            ShipmentStatus status
    ) {
        return Shipment.builder()
                .id(55L)
                .shipmentNumber(
                        "CS-20260726-ABC12345"
                )
                .clientUserId(10L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .status(status)
                .build();
    }

    private AuditEventRequest event(
            String action
    ) {
        return new AuditEventRequest(
                10L,
                "ROLE_CLIENT",
                action,
                "SHIPMENT",
                "55",
                "shipment-service",
                "Shipment audit event",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/shipments",
                201
        );
    }
}
