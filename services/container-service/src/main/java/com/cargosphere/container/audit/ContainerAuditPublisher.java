package com.cargosphere.container.audit;

import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.security.ContainerActorProvider;
import com.cargosphere.container.security.CurrentActor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContainerAuditPublisher {

    private final AuditClient auditClient;

    private final ContainerAuditEventFactory eventFactory;

    private final ContainerActorProvider actorProvider;

    public void publishAllocated(
            AllocationResponse allocation
    ) {
        try {
            CurrentActor actor =
                    actorProvider.getCurrentActor();

            AuditEventRequest request =
                    eventFactory.createAllocatedEvent(
                            actor,
                            allocation
                    );

            auditClient.send(request);
        } catch (RuntimeException exception) {
            log.warn(
                    "Container allocation audit publication failed safely; "
                            + "allocationId={} reason={}",
                    allocationIdOf(allocation),
                    safeMessage(exception)
            );
        }
    }

    public void publishReleased(
            Long allocationId,
            Long shipmentId
    ) {
        try {
            CurrentActor actor =
                    actorProvider.getCurrentActor();

            AuditEventRequest request =
                    eventFactory.createReleasedEvent(
                            actor,
                            allocationId,
                            shipmentId
                    );

            auditClient.send(request);
        } catch (RuntimeException exception) {
            log.warn(
                    "Container release audit publication failed safely; "
                            + "allocationId={} reason={}",
                    allocationId,
                    safeMessage(exception)
            );
        }
    }

    private String allocationIdOf(
            AllocationResponse allocation
    ) {
        if (allocation == null
                || allocation.allocationId() == null) {
            return "unknown";
        }

        return String.valueOf(
                allocation.allocationId()
        );
    }

    private String safeMessage(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass()
                    .getSimpleName();
        }

        return message;
    }
}