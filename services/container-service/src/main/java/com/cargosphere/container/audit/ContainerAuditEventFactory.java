package com.cargosphere.container.audit;

import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.security.CurrentActor;
import org.springframework.stereotype.Component;

@Component
public class ContainerAuditEventFactory {

    private static final String SERVICE_NAME =
            "container-service";

    private static final String ENTITY_TYPE =
            "CONTAINER";

    private static final String OUTCOME_SUCCESS =
            "SUCCESS";

    private static final String ACTION_ALLOCATED =
            "CONTAINER_ALLOCATED";

    private static final String ACTION_RELEASED =
            "CONTAINER_RELEASED";

    private static final String CREATE_ENDPOINT =
            "/api/container-allocations";

    private static final String DELETE_ENDPOINT_PREFIX =
            "/api/container-allocations/";

    public AuditEventRequest createAllocatedEvent(
            CurrentActor actor,
            AllocationResponse allocation
    ) {
        requireActor(actor);
        requireAllocation(allocation);

        return new AuditEventRequest(
                actor.userId(),
                actor.role(),
                ACTION_ALLOCATED,
                ENTITY_TYPE,
                String.valueOf(allocation.allocationId()),
                SERVICE_NAME,
                "Container allocation created for shipment "
                        + allocation.shipmentId(),
                OUTCOME_SUCCESS,
                null,
                null,
                "POST",
                CREATE_ENDPOINT,
                201
        );
    }

    public AuditEventRequest createReleasedEvent(
            CurrentActor actor,
            Long allocationId,
            Long shipmentId
    ) {
        requireActor(actor);
        requirePositiveId(allocationId, "Allocation ID");
        requirePositiveId(shipmentId, "Shipment ID");

        return new AuditEventRequest(
                actor.userId(),
                actor.role(),
                ACTION_RELEASED,
                ENTITY_TYPE,
                String.valueOf(allocationId),
                SERVICE_NAME,
                "Container allocation released for shipment "
                        + shipmentId,
                OUTCOME_SUCCESS,
                null,
                null,
                "DELETE",
                DELETE_ENDPOINT_PREFIX + allocationId,
                204
        );
    }

    private void requireActor(CurrentActor actor) {
        if (actor == null) {
            throw new IllegalArgumentException(
                    "Current actor is required"
            );
        }

        requirePositiveId(actor.userId(), "Actor user ID");

        if (actor.role() == null || actor.role().isBlank()) {
            throw new IllegalArgumentException(
                    "Actor role is required"
            );
        }
    }

    private void requireAllocation(
            AllocationResponse allocation
    ) {
        if (allocation == null) {
            throw new IllegalArgumentException(
                    "Allocation response is required"
            );
        }

        requirePositiveId(
                allocation.allocationId(),
                "Allocation ID"
        );

        requirePositiveId(
                allocation.shipmentId(),
                "Shipment ID"
        );
    }

    private void requirePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }
}