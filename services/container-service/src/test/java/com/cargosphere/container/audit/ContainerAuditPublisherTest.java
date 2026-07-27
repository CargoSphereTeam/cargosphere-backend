package com.cargosphere.container.audit;

import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.security.ContainerActorProvider;
import com.cargosphere.container.security.CurrentActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContainerAuditPublisherTest {

    @Mock
    private AuditClient auditClient;

    @Mock
    private ContainerAuditEventFactory eventFactory;

    @Mock
    private ContainerActorProvider actorProvider;

    private ContainerAuditPublisher publisher;

    private CurrentActor actor;

    private AllocationResponse allocationResponse;

    private AuditEventRequest allocatedEvent;

    private AuditEventRequest releasedEvent;

    @BeforeEach
    void setUp() {
        publisher = new ContainerAuditPublisher(
                auditClient,
                eventFactory,
                actorProvider
        );

        actor = new CurrentActor(
                1L,
                "ROLE_ADMIN"
        );

        LocalDateTime now = LocalDateTime.now();

        allocationResponse = new AllocationResponse(
                10L,
                100L,
                5L,
                "20GP",
                "20 Foot General Purpose",
                2,
                "ALLOCATED",
                "Publisher test",
                now,
                now
        );

        allocatedEvent = new AuditEventRequest(
                1L,
                "ROLE_ADMIN",
                "CONTAINER_ALLOCATED",
                "CONTAINER",
                "10",
                "container-service",
                "Container allocation created for shipment 100",
                "SUCCESS",
                null,
                null,
                "POST",
                "/api/container-allocations",
                201
        );

        releasedEvent = new AuditEventRequest(
                1L,
                "ROLE_ADMIN",
                "CONTAINER_RELEASED",
                "CONTAINER",
                "10",
                "container-service",
                "Container allocation released for shipment 100",
                "SUCCESS",
                null,
                null,
                "DELETE",
                "/api/container-allocations/10",
                204
        );
    }

    @Test
    void publishAllocated_shouldBuildAndSendEvent() {
        when(actorProvider.getCurrentActor())
                .thenReturn(actor);

        when(eventFactory.createAllocatedEvent(
                actor,
                allocationResponse
        )).thenReturn(allocatedEvent);

        publisher.publishAllocated(allocationResponse);

        verify(actorProvider).getCurrentActor();

        verify(eventFactory).createAllocatedEvent(
                actor,
                allocationResponse
        );

        verify(auditClient).send(allocatedEvent);
    }

    @Test
    void publishReleased_shouldBuildAndSendEvent() {
        when(actorProvider.getCurrentActor())
                .thenReturn(actor);

        when(eventFactory.createReleasedEvent(
                actor,
                10L,
                100L
        )).thenReturn(releasedEvent);

        publisher.publishReleased(
                10L,
                100L
        );

        verify(actorProvider).getCurrentActor();

        verify(eventFactory).createReleasedEvent(
                actor,
                10L,
                100L
        );

        verify(auditClient).send(releasedEvent);
    }

    @Test
    void publishAllocated_whenActorProviderFails_shouldNotThrow() {
        when(actorProvider.getCurrentActor())
                .thenThrow(
                        new IllegalStateException(
                                "Authenticated JWT actor is required"
                        )
                );

        assertDoesNotThrow(
                () -> publisher.publishAllocated(
                        allocationResponse
                )
        );

        verifyNoInteractions(
                eventFactory,
                auditClient
        );
    }

    @Test
    void publishAllocated_whenFactoryFails_shouldNotThrow() {
        when(actorProvider.getCurrentActor())
                .thenReturn(actor);

        when(eventFactory.createAllocatedEvent(
                actor,
                allocationResponse
        )).thenThrow(
                new IllegalArgumentException(
                        "Allocation ID must be positive"
                )
        );

        assertDoesNotThrow(
                () -> publisher.publishAllocated(
                        allocationResponse
                )
        );

        verifyNoInteractions(auditClient);
    }

    @Test
    void publishAllocated_whenAuditClientFails_shouldNotThrow() {
        when(actorProvider.getCurrentActor())
                .thenReturn(actor);

        when(eventFactory.createAllocatedEvent(
                actor,
                allocationResponse
        )).thenReturn(allocatedEvent);

        doThrow(
                new RuntimeException(
                        "Audit service unavailable"
                )
        ).when(auditClient).send(allocatedEvent);

        assertDoesNotThrow(
                () -> publisher.publishAllocated(
                        allocationResponse
                )
        );

        verify(auditClient).send(allocatedEvent);
    }

    @Test
    void publishReleased_whenAuditClientFails_shouldNotThrow() {
        when(actorProvider.getCurrentActor())
                .thenReturn(actor);

        when(eventFactory.createReleasedEvent(
                actor,
                10L,
                100L
        )).thenReturn(releasedEvent);

        doThrow(
                new RuntimeException(
                        "Audit service unavailable"
                )
        ).when(auditClient).send(releasedEvent);

        assertDoesNotThrow(
                () -> publisher.publishReleased(
                        10L,
                        100L
                )
        );

        verify(auditClient).send(releasedEvent);
    }

    @Test
    void publishAllocated_whenAllocationIsNull_shouldNotThrow() {
        when(actorProvider.getCurrentActor())
                .thenReturn(actor);

        when(eventFactory.createAllocatedEvent(
                actor,
                null
        )).thenThrow(
                new IllegalArgumentException(
                        "Allocation response is required"
                )
        );

        assertDoesNotThrow(
                () -> publisher.publishAllocated(null)
        );

        verifyNoInteractions(auditClient);
    }
}