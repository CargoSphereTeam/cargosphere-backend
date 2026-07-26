package com.cargosphere.container.audit;

import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.security.CurrentActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContainerAuditEventFactoryTest {

    private ContainerAuditEventFactory eventFactory;

    private CurrentActor actor;

    private AllocationResponse allocationResponse;

    @BeforeEach
    void setUp() {
        eventFactory = new ContainerAuditEventFactory();

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
                "Factory test",
                now,
                now
        );
    }

    @Test
    void createAllocatedEvent_shouldCreateExpectedRequest() {
        AuditEventRequest request =
                eventFactory.createAllocatedEvent(
                        actor,
                        allocationResponse
                );

        assertEquals(1L, request.actorUserId());
        assertEquals("ROLE_ADMIN", request.actorRole());
        assertEquals("CONTAINER_ALLOCATED", request.action());
        assertEquals("CONTAINER", request.entityType());
        assertEquals("10", request.entityId());
        assertEquals("container-service", request.serviceName());
        assertEquals(
                "Container allocation created for shipment 100",
                request.description()
        );
        assertEquals("SUCCESS", request.outcome());
        assertNull(request.requestId());
        assertNull(request.ipAddress());
        assertEquals("POST", request.httpMethod());
        assertEquals(
                "/api/container-allocations",
                request.endpoint()
        );
        assertEquals(201, request.statusCode());
    }

    @Test
    void createReleasedEvent_shouldCreateExpectedRequest() {
        AuditEventRequest request =
                eventFactory.createReleasedEvent(
                        actor,
                        10L,
                        100L
                );

        assertEquals(1L, request.actorUserId());
        assertEquals("ROLE_ADMIN", request.actorRole());
        assertEquals("CONTAINER_RELEASED", request.action());
        assertEquals("CONTAINER", request.entityType());
        assertEquals("10", request.entityId());
        assertEquals("container-service", request.serviceName());
        assertEquals(
                "Container allocation released for shipment 100",
                request.description()
        );
        assertEquals("SUCCESS", request.outcome());
        assertNull(request.requestId());
        assertNull(request.ipAddress());
        assertEquals("DELETE", request.httpMethod());
        assertEquals(
                "/api/container-allocations/10",
                request.endpoint()
        );
        assertEquals(204, request.statusCode());
    }

    @Test
    void createAllocatedEvent_whenActorMissing_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventFactory.createAllocatedEvent(
                        null,
                        allocationResponse
                )
        );

        assertEquals(
                "Current actor is required",
                exception.getMessage()
        );
    }

    @Test
    void createAllocatedEvent_whenAllocationMissing_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventFactory.createAllocatedEvent(
                        actor,
                        null
                )
        );

        assertEquals(
                "Allocation response is required",
                exception.getMessage()
        );
    }

    @Test
    void createAllocatedEvent_whenAllocationIdInvalid_shouldThrowException() {
        AllocationResponse invalidResponse =
                new AllocationResponse(
                        0L,
                        100L,
                        5L,
                        "20GP",
                        "20 Foot General Purpose",
                        2,
                        "ALLOCATED",
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventFactory.createAllocatedEvent(
                        actor,
                        invalidResponse
                )
        );

        assertEquals(
                "Allocation ID must be positive",
                exception.getMessage()
        );
    }

    @Test
    void createReleasedEvent_whenShipmentIdInvalid_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventFactory.createReleasedEvent(
                        actor,
                        10L,
                        0L
                )
        );

        assertEquals(
                "Shipment ID must be positive",
                exception.getMessage()
        );
    }
}