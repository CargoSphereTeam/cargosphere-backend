package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.ShipmentResponse;
import com.cargosphere.shipment.dto.UpdateShipmentStatusRequest;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import com.cargosphere.shipment.mapper.CargoDetailMapper;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.mapper.ShipmentMapper;
import com.cargosphere.shipment.repository.CargoDetailRepository;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceAuditIntegrationTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CargoDetailRepository cargoDetailRepository;

    @Mock
    private ShipmentEventRepository
            shipmentEventRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private CargoDetailMapper cargoDetailMapper;

    @Mock
    private ShipmentEventMapper shipmentEventMapper;

    @Mock
    private ShipmentAuditPublisher
            shipmentAuditPublisher;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService.setShipmentAuditPublisher(
                shipmentAuditPublisher
        );
    }

    @Test
    void createShipmentShouldPublishAuditEvent() {
        CreateShipmentRequest request =
                CreateShipmentRequest.builder()
                        .clientUserId(10L)
                        .originLocation("Mumbai")
                        .destinationLocation("Pune")
                        .shipmentType(
                                ShipmentType.ROAD
                        )
                        .build();

        Shipment shipment =
                shipment(ShipmentStatus.CREATED);

        ShipmentEvent shipmentEvent =
                new ShipmentEvent();

        ShipmentResponse response =
                ShipmentResponse.builder()
                        .id(55L)
                        .status(
                                ShipmentStatus.CREATED
                        )
                        .build();

        when(shipmentRepository
                .existsByShipmentNumber(
                        anyString()
                ))
                .thenReturn(false);

        when(shipmentMapper.toEntity(
                any(CreateShipmentRequest.class),
                anyString()
        )).thenReturn(shipment);

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventMapper.toEntity(
                shipment,
                ShipmentEventType.CREATED,
                "Shipment created",
                "Mumbai"
        )).thenReturn(shipmentEvent);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(response);

        shipmentService.createShipment(request);

        verify(shipmentAuditPublisher)
                .publishShipmentCreated(shipment);
    }

    @Test
    void updateStatusShouldPublishPreviousStatus() {
        Shipment shipment =
                shipment(ShipmentStatus.CREATED);

        ShipmentEvent shipmentEvent =
                new ShipmentEvent();

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest
                        .builder()
                        .status(
                                ShipmentStatus.BOOKED
                        )
                        .build();

        ShipmentResponse response =
                ShipmentResponse.builder()
                        .id(55L)
                        .status(
                                ShipmentStatus.BOOKED
                        )
                        .build();

        when(shipmentRepository.findById(55L))
                .thenReturn(
                        Optional.of(shipment)
                );

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventMapper.toEntity(
                shipment,
                ShipmentEventType.BOOKED,
                "Shipment status updated to BOOKED",
                null
        )).thenReturn(shipmentEvent);

        when(shipmentMapper.toResponse(shipment))
                .thenReturn(response);

        shipmentService.updateShipmentStatus(
                55L,
                request
        );

        verify(shipmentAuditPublisher)
                .publishShipmentStatusUpdated(
                        shipment,
                        ShipmentStatus.CREATED
                );
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
                .shipmentType(
                        ShipmentType.ROAD
                )
                .status(status)
                .build();
    }
}