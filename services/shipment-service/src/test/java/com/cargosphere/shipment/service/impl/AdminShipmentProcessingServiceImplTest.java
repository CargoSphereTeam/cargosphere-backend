package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.exception.InvalidProcessingStageException;
import com.cargosphere.shipment.exception.ResourceNotFoundException;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminShipmentProcessingServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentEventRepository shipmentEventRepository;

    @Mock
    private ShipmentAuditPublisher shipmentAuditPublisher;

    private AdminShipmentProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminShipmentProcessingServiceImpl(
                shipmentRepository,
                shipmentEventRepository,
                new ShipmentEventMapper(),
                shipmentAuditPublisher
        );
    }

    @Test
    void shouldStartAdminProcessingSuccessfully() {
        Shipment shipment = createShipment(
                ProcessingStage.PENDING_ADMIN_REVIEW
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.ADMIN_PROCESSING_STARTED
                ))
                .thenReturn(false);

        ProcessingStartResponse response =
                service.startProcessing(10L);

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        assertThat(shipment.getProcessingStartedAt())
                .isNotNull();

        assertThat(
                shipment.getProcessingStartedAt().getOffset()
        ).isEqualTo(ZoneOffset.UTC);

        assertThat(response.getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getShipmentNumber())
                .isEqualTo("SHP-2026-00010");

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        assertThat(response.getProcessingStartedAt())
                .isEqualTo(
                        shipment.getProcessingStartedAt()
                );

        verify(shipmentRepository)
                .save(shipment);

        ArgumentCaptor<ShipmentEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        ShipmentEvent.class
                );

        verify(shipmentEventRepository)
                .save(eventCaptor.capture());

        assertThat(eventCaptor.getValue().getEventType())
                .isEqualTo(
                        ShipmentEventType.ADMIN_PROCESSING_STARTED
                );

        assertThat(eventCaptor.getValue().getShipment())
                .isSameAs(shipment);

        verify(shipmentAuditPublisher)
                .publishAdminProcessingStarted(shipment);
    }

    @Test
    void shouldRejectRepeatedProcessingStart() {
        Shipment shipment = createShipment(
                ProcessingStage.CONTAINER_ALLOCATION
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
                service.startProcessing(10L)
        )
                .isInstanceOf(
                        InvalidProcessingStageException.class
                )
                .hasMessageContaining(
                        "PENDING_ADMIN_REVIEW"
                )
                .hasMessageContaining(
                        "CONTAINER_ALLOCATION"
                );

        verify(shipmentRepository, never())
                .save(shipment);

        verify(shipmentEventRepository, never())
                .save(
                        org.mockito.ArgumentMatchers
                                .any(ShipmentEvent.class)
                );

        verify(shipmentAuditPublisher, never())
                .publishAdminProcessingStarted(shipment);
    }

    @Test
    void shouldReturnNotFoundWhenShipmentDoesNotExist() {
        when(shipmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.startProcessing(99L)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessageContaining(
                        "Shipment not found with id: 99"
                );

        verify(shipmentRepository, never())
                .save(
                        org.mockito.ArgumentMatchers
                                .any(Shipment.class)
                );

        verifyNoProcessingSideEffects();
    }

    @Test
    void shouldNotCreateDuplicateProcessingStartedEvent() {
        Shipment shipment = createShipment(
                ProcessingStage.PENDING_ADMIN_REVIEW
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.ADMIN_PROCESSING_STARTED
                ))
                .thenReturn(true);

        ProcessingStartResponse response =
                service.startProcessing(10L);

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        verify(shipmentEventRepository, never())
                .save(
                        org.mockito.ArgumentMatchers
                                .any(ShipmentEvent.class)
                );

        verify(shipmentAuditPublisher)
                .publishAdminProcessingStarted(shipment);
    }

    private Shipment createShipment(
            ProcessingStage processingStage
    ) {
        return Shipment.builder()
                .id(10L)
                .shipmentNumber("SHP-2026-00010")
                .clientUserId(100L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .processingStage(processingStage)
                .build();
    }

    private void verifyNoProcessingSideEffects() {
        verify(shipmentEventRepository, never())
                .save(
                        org.mockito.ArgumentMatchers
                                .any(ShipmentEvent.class)
                );

        verify(shipmentAuditPublisher, never())
                .publishAdminProcessingStarted(
                        org.mockito.ArgumentMatchers
                                .any(Shipment.class)
                );
    }
}