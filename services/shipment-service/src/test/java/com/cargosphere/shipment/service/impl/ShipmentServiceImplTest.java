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
import com.cargosphere.shipment.exception.InvalidShipmentOperationException;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
        CreateShipmentRequest request = createValidShipmentRequest();

        when(shipmentRepository.existsByShipmentNumber(anyString()))
                .thenReturn(false);

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> {
                    Shipment shipment = invocation.getArgument(0);

                    OffsetDateTime timestamp =
                            OffsetDateTime.now(ZoneOffset.UTC);

                    shipment.setId(1L);
                    shipment.setCreatedAt(timestamp);
                    shipment.setUpdatedAt(timestamp);

                    return shipment;
                });

        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> {
                    ShipmentEvent event = invocation.getArgument(0);
                    event.setId(1L);
                    return event;
                });

        ShipmentResponse response =
                shipmentService.createShipment(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getClientUserId());
        assertEquals("Mumbai", response.getOriginLocation());
        assertEquals("Pune", response.getDestinationLocation());
        assertEquals(ShipmentType.ROAD, response.getShipmentType());
        assertEquals(ShipmentStatus.CREATED, response.getStatus());
        assertTrue(response.getShipmentNumber().startsWith("CS-"));

        ArgumentCaptor<ShipmentEvent> eventCaptor =
                ArgumentCaptor.forClass(ShipmentEvent.class);

        verify(shipmentEventRepository).save(eventCaptor.capture());

        ShipmentEvent savedEvent = eventCaptor.getValue();

        assertEquals(
                ShipmentEventType.CREATED,
                savedEvent.getEventType()
        );
        assertEquals(
                "Shipment created",
                savedEvent.getEventDescription()
        );
        assertEquals(
                "Mumbai",
                savedEvent.getEventLocation()
        );
        assertEquals(
                1L,
                savedEvent.getShipment().getId()
        );
    }

    @Test
    void createShipmentShouldRejectSameOriginAndDestination() {
        CreateShipmentRequest request =
                createValidShipmentRequest();

        request.setDestinationLocation("  MUMBAI  ");

        InvalidShipmentOperationException exception =
                assertThrows(
                        InvalidShipmentOperationException.class,
                        () -> shipmentService.createShipment(request)
                );

        assertEquals(
                "Origin and destination locations cannot be the same",
                exception.getMessage()
        );

        verifyNoInteractions(
                shipmentRepository,
                cargoDetailRepository,
                shipmentEventRepository
        );
    }

    @Test
    void createShipmentShouldRejectDeliveryDateBeforePickupDate() {
        CreateShipmentRequest request =
                createValidShipmentRequest();

        request.setExpectedPickupDate(
                LocalDate.now().plusDays(10)
        );

        request.setExpectedDeliveryDate(
                LocalDate.now().plusDays(5)
        );

        InvalidShipmentOperationException exception =
                assertThrows(
                        InvalidShipmentOperationException.class,
                        () -> shipmentService.createShipment(request)
                );

        assertEquals(
                "Expected delivery date cannot be before expected pickup date",
                exception.getMessage()
        );

        verifyNoInteractions(
                shipmentRepository,
                cargoDetailRepository,
                shipmentEventRepository
        );
    }

    @Test
    void getShipmentByIdShouldReturnShipmentWhenExists() {
        Shipment shipment =
                createSampleShipment(ShipmentStatus.CREATED);

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        ShipmentResponse response =
                shipmentService.getShipmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(
                "CS-20260719-ABC12345",
                response.getShipmentNumber()
        );
        assertEquals(
                ShipmentStatus.CREATED,
                response.getStatus()
        );
    }

    @Test
    void getShipmentByIdShouldThrowExceptionWhenShipmentDoesNotExist() {
        when(shipmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> shipmentService.getShipmentById(999L)
                );

        assertEquals(
                "Shipment not found with id: 999",
                exception.getMessage()
        );
    }

    @Test
    void addCargoDetailShouldSaveCargoDetailAndCreateCargoAddedEvent() {
        Shipment shipment =
                createSampleShipment(ShipmentStatus.CREATED);

        CargoDetailRequest request =
                createCargoDetailRequest();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        when(cargoDetailRepository.save(any(CargoDetail.class)))
                .thenAnswer(invocation -> {
                    CargoDetail cargoDetail =
                            invocation.getArgument(0);

                    cargoDetail.setId(1L);
                    cargoDetail.setCreatedAt(LocalDateTime.now());
                    cargoDetail.setUpdatedAt(LocalDateTime.now());

                    return cargoDetail;
                });

        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> {
                    ShipmentEvent event =
                            invocation.getArgument(0);

                    event.setId(2L);

                    return event;
                });

        CargoDetailResponse response =
                shipmentService.addCargoDetail(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getShipmentId());
        assertEquals(
                "Electronics Box",
                response.getCargoName()
        );
        assertEquals(
                CargoType.ELECTRONICS,
                response.getCargoType()
        );
        assertTrue(response.getFragile());
        assertFalse(response.getHazardous());

        ArgumentCaptor<ShipmentEvent> eventCaptor =
                ArgumentCaptor.forClass(ShipmentEvent.class);

        verify(shipmentEventRepository).save(eventCaptor.capture());

        ShipmentEvent savedEvent =
                eventCaptor.getValue();

        assertEquals(
                ShipmentEventType.CARGO_ADDED,
                savedEvent.getEventType()
        );
        assertEquals(
                "Cargo added: Electronics Box",
                savedEvent.getEventDescription()
        );
        assertEquals(
                "Mumbai",
                savedEvent.getEventLocation()
        );
    }

    @Test
    void addCargoDetailShouldRejectCargoForInTransitShipment() {
        Shipment shipment =
                createSampleShipment(
                        ShipmentStatus.IN_TRANSIT
                );

        CargoDetailRequest request =
                createCargoDetailRequest();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        InvalidShipmentOperationException exception =
                assertThrows(
                        InvalidShipmentOperationException.class,
                        () -> shipmentService.addCargoDetail(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Cargo cannot be added when shipment status is IN_TRANSIT",
                exception.getMessage()
        );

        verify(cargoDetailRepository, never())
                .save(any(CargoDetail.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }

    @Test
    void updateShipmentStatusShouldAllowCreatedToBooked() {
        Shipment shipment =
                createSampleShipment(ShipmentStatus.CREATED);

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.BOOKED)
                        .build();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        when(shipmentEventRepository.save(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> {
                    ShipmentEvent event =
                            invocation.getArgument(0);

                    event.setId(3L);

                    return event;
                });

        ShipmentResponse response =
                shipmentService.updateShipmentStatus(
                        1L,
                        request
                );

        assertNotNull(response);
        assertEquals(
                ShipmentStatus.BOOKED,
                response.getStatus()
        );

        ArgumentCaptor<ShipmentEvent> eventCaptor =
                ArgumentCaptor.forClass(ShipmentEvent.class);

        verify(shipmentEventRepository).save(eventCaptor.capture());

        ShipmentEvent savedEvent =
                eventCaptor.getValue();

        assertEquals(
                ShipmentEventType.BOOKED,
                savedEvent.getEventType()
        );
        assertEquals(
                "Shipment status updated to BOOKED",
                savedEvent.getEventDescription()
        );
    }

    @Test
    void updateShipmentStatusShouldRejectSameStatus() {
        Shipment shipment =
                createSampleShipment(ShipmentStatus.BOOKED);

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.BOOKED)
                        .build();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        InvalidShipmentOperationException exception =
                assertThrows(
                        InvalidShipmentOperationException.class,
                        () -> shipmentService.updateShipmentStatus(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Shipment is already in status BOOKED",
                exception.getMessage()
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }

    @Test
    void updateShipmentStatusShouldRejectCreatedToInTransit() {
        Shipment shipment =
                createSampleShipment(ShipmentStatus.CREATED);

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.IN_TRANSIT)
                        .build();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        InvalidShipmentOperationException exception =
                assertThrows(
                        InvalidShipmentOperationException.class,
                        () -> shipmentService.updateShipmentStatus(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Invalid shipment status transition from CREATED to IN_TRANSIT",
                exception.getMessage()
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }

    @Test
    void updateShipmentStatusShouldRejectChangesAfterDelivered() {
        Shipment shipment =
                createSampleShipment(
                        ShipmentStatus.DELIVERED
                );

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.CANCELLED)
                        .build();

        when(shipmentRepository.findById(1L))
                .thenReturn(Optional.of(shipment));

        InvalidShipmentOperationException exception =
                assertThrows(
                        InvalidShipmentOperationException.class,
                        () -> shipmentService.updateShipmentStatus(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Invalid shipment status transition from DELIVERED to CANCELLED",
                exception.getMessage()
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }

    @Test
    void getShipmentEventsByShipmentIdShouldReturnShipmentEvents() {
        Shipment shipment =
                createSampleShipment(ShipmentStatus.CREATED);

        ShipmentEvent event =
                ShipmentEvent.builder()
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

        when(shipmentEventRepository
                .findByShipment_IdOrderByEventTimeDesc(1L))
                .thenReturn(List.of(event));

        List<ShipmentEventResponse> responses =
                shipmentService
                        .getShipmentEventsByShipmentId(1L);

        assertEquals(1, responses.size());
        assertEquals(
                ShipmentEventType.CREATED,
                responses.get(0).getEventType()
        );
        assertEquals(
                "Shipment created",
                responses.get(0).getEventDescription()
        );
    }

    private CreateShipmentRequest createValidShipmentRequest() {
        return CreateShipmentRequest.builder()
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .expectedPickupDate(
                        LocalDate.now().plusDays(5)
                )
                .expectedDeliveryDate(
                        LocalDate.now().plusDays(10)
                )
                .build();
    }

    private CargoDetailRequest createCargoDetailRequest() {
        return CargoDetailRequest.builder()
                .cargoName("Electronics Box")
                .cargoDescription("Laptop accessories")
                .cargoType(CargoType.ELECTRONICS)
                .weightKg(BigDecimal.valueOf(25.50))
                .volumeCbm(BigDecimal.valueOf(1.20))
                .quantity(2)
                .fragile(true)
                .hazardous(false)
                .build();
    }

    private Shipment createSampleShipment(
            ShipmentStatus status
    ) {
        OffsetDateTime timestamp =
                OffsetDateTime.now(ZoneOffset.UTC);

        Shipment shipment = Shipment.builder()
                .shipmentNumber("CS-20260719-ABC12345")
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .status(status)
                .expectedPickupDate(
                        LocalDate.now().plusDays(5)
                )
                .expectedDeliveryDate(
                        LocalDate.now().plusDays(10)
                )
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        shipment.setId(1L);

        return shipment;
    }
}