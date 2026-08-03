package com.cargosphere.shipment.audit;

import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import org.springframework.stereotype.Component;

@Component
public class ShipmentAuditEventFactory {

    private static final String SERVICE_NAME =
            "shipment-service";

    private static final String ENTITY_TYPE =
            "SHIPMENT";

    private static final String SUCCESS =
            "SUCCESS";

    public AuditEventRequest shipmentCreated(
            Shipment shipment,
            CurrentActor actor
    ) {
        return create(
                shipment,
                actor,
                "SHIPMENT_CREATED",
                "Shipment created successfully",
                "POST",
                "/api/shipments",
                201
        );
    }

    public AuditEventRequest shipmentStatusUpdated(
            Shipment shipment,
            ShipmentStatus previousStatus,
            CurrentActor actor
    ) {
        String description =
                "Shipment status changed from "
                        + previousStatus
                        + " to "
                        + shipment.getStatus();

        return create(
                shipment,
                actor,
                "SHIPMENT_STATUS_UPDATED",
                description,
                "PATCH",
                "/api/shipments/"
                        + shipment.getId()
                        + "/status",
                200
        );
    }

    public AuditEventRequest cargoVerified(
            Shipment shipment,
            CurrentActor actor
    ) {
        return create(
                shipment,
                actor,
                "CARGO_VERIFIED",
                "Shipment cargo verification confirmed successfully",
                "PUT",
                "/api/admin/shipments/"
                        + shipment.getId()
                        + "/cargo-verification",
                200
        );
    }

    public AuditEventRequest adminProcessingStarted(
            Shipment shipment,
            CurrentActor actor
    ) {
        return create(
                shipment,
                actor,
                "ADMIN_PROCESSING_STARTED",
                "Administrative shipment processing started",
                "POST",
                "/api/admin/shipments/"
                        + shipment.getId()
                        + "/processing/start",
                200
        );
    }

    public AuditEventRequest processingAdvanced(
            Shipment shipment,
            ProcessingStage previousStage,
            CurrentActor actor
    ) {
        String description =
                "Shipment processing advanced from "
                        + previousStage
                        + " to "
                        + shipment.getProcessingStage();

        return create(
                shipment,
                actor,
                "SHIPMENT_PROCESSING_ADVANCED",
                description,
                "POST",
                "/api/admin/shipments/"
                        + shipment.getId()
                        + "/processing/continue",
                200
        );
    }
    public AuditEventRequest ebillGenerated(
            Shipment shipment,
            CurrentActor actor
    ) {
        String description =
                "Shipment eBill generated successfully with number "
                        + shipment.getEbillNumber();

        return create(
                shipment,
                actor,
                "EBILL_GENERATED",
                description,
                "POST",
                "/api/admin/shipments/"
                        + shipment.getId()
                        + "/ebill",
                201
        );
    }

    private AuditEventRequest create(
            Shipment shipment,
            CurrentActor actor,
            String action,
            String description,
            String httpMethod,
            String endpoint,
            int statusCode
    ) {
        CurrentActor safeActor =
                actor == null
                        ? CurrentActor.anonymous()
                        : actor;

        return new AuditEventRequest(
                safeActor.userId(),
                safeActor.role(),
                action,
                ENTITY_TYPE,
                shipment.getId() == null
                        ? null
                        : shipment.getId().toString(),
                SERVICE_NAME,
                description,
                SUCCESS,
                null,
                null,
                httpMethod,
                endpoint,
                statusCode
        );
    }
}
