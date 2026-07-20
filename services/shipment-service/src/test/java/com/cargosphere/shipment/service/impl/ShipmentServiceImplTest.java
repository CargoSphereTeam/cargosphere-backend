package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.dto.CargoDetailRequest;
import com.cargosphere.shipment.dto.CargoDetailResponse;
import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.ShipmentEventResponse;
import com.cargosphere.shipment.dto.ShipmentResponse;
import com.cargosphere.shipment.dto.UpdateShipmentStatusRequest;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import com.cargosphere.shipment.exception.ResourceNotFoundException;
import com.cargosphere.shipment.mapper.CargoDetailMapper;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.mapper.ShipmentMapper;
import com.cargosphere.shipment.repository.CargoDetailRepository;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CargoDetailRepository cargoDetailRepository;

    @Mock
    private ShipmentEventRepository shipmentEventRepository;

    private ShipmentServiceImpl shipmentService;

    @BeforeEach
    void setUp() {
        ShipmentMapper shipmentMapper = new ShipmentMapper();
        CargoDetailMapper cargoDetailMapper = new CargoDetailMapper();
        ShipmentEventMapper shipmentEventMapper = new ShipmentEventMapper();

        shipmentService = new ShipmentServiceImpl(
                shipmentRepository,
                cargoDetailRepository,
                shipmentEventRepository,
                shipmentMapper,
                cargoDetailMapper,
                shipmentEventMapper
        );
    }

    @Test
    void createShipmentShouldSaveShipmentAndCreateCreatedEvent() {
        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .expectedPickupDate(LocalDate.of(2026, 8, 1))
                .expectedDeliveryDate(LocalDate.of(2026, 8, 5))
                .build();

        when(shipmentRepository.existsByShipmentNumber(anyString()))
                .thenReturn(false);

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> {
                    Shipment shipment = invocation.getArgument(0);
                    shipment.setId(1L);
                    shipment.setCreatedAt(LocalDateTime.now());
                    shipment.setUpdatedAt(LocalDateTime.now());
                    return shipment;
                });

        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> {
                    ShipmentEvent event = invocation.getArgument(0);
                    event.setId(1L);
                    return event;
                });

        ShipmentResponse response = shipmentService.createShipment(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getClientUserId());
        assertEquals("Mumbai", response.getOriginLocation());
        assertEquals("Pune", response.getDestinationLocation());
        assertEquals(ShipmentType.ROAD, response.getShipmentType());
        assertEquals(ShipmentStatus.CREATED, response.getStatus());
        assertTrue(response.getShipmentNumber().startsWith("CS-"));

        ArgumentCaptor<ShipmentEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(eventCaptor.capture());

        ShipmentEvent savedEvent = eventCaptor.getValue();

        assertEquals(ShipmentEventType.CREATED, savedEvent.getEventType());
        assertEquals("Shipment created", savedEvent.getEventDescription());
        assertEquals("Mumbai", savedEvent.getEventLocation());
        assertEquals(1L, savedEvent.getShipment().getId());
    }

    @Test
    void getShipmentByIdShouldReturnShipmentWhenExists() {
        Shipment shipment = createSampleShipment();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        ShipmentResponse response = shipmentService.getShipmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CS-20260719-ABC12345", response.getShipmentNumber());
        assertEquals(ShipmentStatus.CREATED, response.getStatus());
    }

    @Test
    void getShipmentByIdShouldThrowExceptionWhenShipmentDoesNotExist() {
        when(shipmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> shipmentService.getShipmentById(999L)
        );

        assertEquals("Shipment not found with id: 999", exception.getMessage());
    }

    @Test
    void addCargoDetailShouldSaveCargoDetailAndCreateCargoAddedEvent() {
        Shipment shipment = createSampleShipment();

        CargoDetailRequest request = CargoDetailRequest.builder()
                .cargoName("Electronics Box")
                .cargoDescription("Laptop accessories")
                .cargoType(CargoType.ELECTRONICS)
                .weightKg(BigDecimal.valueOf(25.50))
                .volumeCbm(BigDecimal.valueOf(1.20))
                .quantity(2)
                .fragile(true)
                .hazardous(false)
                .build();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        when(cargoDetailRepository.save(any(CargoDetail.class)))
                .thenAnswer(invocation -> {
                    CargoDetail cargoDetail = invocation.getArgument(0);
                    cargoDetail.setId(1L);
                    cargoDetail.setCreatedAt(LocalDateTime.now());
                    cargoDetail.setUpdatedAt(LocalDateTime.now());
                    return cargoDetail;
                });

        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> {
                    ShipmentEvent event = invocation.getArgument(0);
                    event.setId(2L);
                    return event;
                });

        CargoDetailResponse response = shipmentService.addCargoDetail(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getShipmentId());
        assertEquals("Electronics Box", response.getCargoName());
        assertEquals(CargoType.ELECTRONICS, response.getCargoType());
        assertTrue(response.getFragile());
        assertFalse(response.getHazardous());

        ArgumentCaptor<ShipmentEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(eventCaptor.capture());

        ShipmentEvent savedEvent = eventCaptor.getValue();

        assertEquals(ShipmentEventType.CARGO_ADDED, savedEvent.getEventType());
        assertEquals("Cargo added: Electronics Box", savedEvent.getEventDescription());
        assertEquals("Mumbai", savedEvent.getEventLocation());
    }

    @Test
    void updateShipmentStatusShouldUpdateStatusAndCreateStatusEvent() {
        Shipment shipment = createSampleShipment();

        UpdateShipmentStatusRequest request = UpdateShipmentStatusRequest.builder()
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> {
                    ShipmentEvent event = invocation.getArgument(0);
                    event.setId(3L);
                    return event;
                });

        ShipmentResponse response = shipmentService.updateShipmentStatus(1L, request);

        assertNotNull(response);
        assertEquals(ShipmentStatus.IN_TRANSIT, response.getStatus());

        ArgumentCaptor<ShipmentEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);
        verify(shipmentEventRepository).save(eventCaptor.capture());

        ShipmentEvent savedEvent = eventCaptor.getValue();

        assertEquals(ShipmentEventType.IN_TRANSIT, savedEvent.getEventType());
        assertEquals("Shipment status updated to IN_TRANSIT", savedEvent.getEventDescription());
    }

    @Test
    void getShipmentEventsByShipmentIdShouldReturnShipmentEvents() {
        Shipment shipment = createSampleShipment();

        ShipmentEvent event = ShipmentEvent.builder()
                .shipment(shipment)
                .eventType(ShipmentEventType.CREATED)
                .eventDescription("Shipment created")
                .eventLocation("Mumbai")
                .eventTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        event.setId(1L);

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        when(shipmentEventRepository.findByShipment_IdOrderByEventTimeDesc(1L))
                .thenReturn(List.of(event));

        List<ShipmentEventResponse> responses = shipmentService.getShipmentEventsByShipmentId(1L);

        assertEquals(1, responses.size());
        assertEquals(ShipmentEventType.CREATED, responses.get(0).getEventType());
        assertEquals("Shipment created", responses.get(0).getEventDescription());
    }

    private Shipment createSampleShipment() {
        Shipment shipment = Shipment.builder()
                .shipmentNumber("CS-20260719-ABC12345")
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .status(ShipmentStatus.CREATED)
                .expectedPickupDate(LocalDate.of(2026, 8, 1))
                .expectedDeliveryDate(LocalDate.of(2026, 8, 5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        shipment.setId(1L);

        return shipment;
    }
}