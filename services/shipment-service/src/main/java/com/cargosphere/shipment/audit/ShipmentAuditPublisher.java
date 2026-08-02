package com.cargosphere.shipment.audit;

import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentAuditPublisher {

    private final AuditClient auditClient;

    private final ShipmentAuditEventFactory
            shipmentAuditEventFactory;

    private final ShipmentActorProvider
            shipmentActorProvider;

    public void publishShipmentCreated(
            Shipment shipment
    ) {
        CurrentActor actor =
                shipmentActorProvider.getCurrentActor();

        AuditEventRequest event =
                shipmentAuditEventFactory.shipmentCreated(
                        shipment,
                        actor
                );

        auditClient.publish(event);
    }

    public void publishShipmentStatusUpdated(
            Shipment shipment,
            ShipmentStatus previousStatus
    ) {
        CurrentActor actor =
                shipmentActorProvider.getCurrentActor();

        AuditEventRequest event =
                shipmentAuditEventFactory
                        .shipmentStatusUpdated(
                                shipment,
                                previousStatus,
                                actor
                        );

        auditClient.publish(event);
    }

    public void publishCargoVerified(
            Shipment shipment
    ) {
        CurrentActor actor =
                shipmentActorProvider.getCurrentActor();

        AuditEventRequest event =
                shipmentAuditEventFactory.cargoVerified(
                        shipment,
                        actor
                );

        auditClient.publish(event);
    }

    public void publishAdminProcessingStarted(
            Shipment shipment
    ) {
        CurrentActor actor =
                shipmentActorProvider.getCurrentActor();

        AuditEventRequest event =
                shipmentAuditEventFactory
                        .adminProcessingStarted(
                                shipment,
                                actor
                        );

        auditClient.publish(event);
    }
}