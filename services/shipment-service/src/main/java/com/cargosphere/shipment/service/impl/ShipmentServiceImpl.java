package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.dto.*;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.exception.InvalidShipmentOperationException;
import com.cargosphere.shipment.exception.ResourceNotFoundException;
import com.cargosphere.shipment.mapper.CargoDetailMapper;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.mapper.ShipmentMapper;
import com.cargosphere.shipment.repository.CargoDetailRepository;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import com.cargosphere.shipment.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CargoDetailRepository cargoDetailRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final ShipmentMapper shipmentMapper;
    private final CargoDetailMapper cargoDetailMapper;
    private final ShipmentEventMapper shipmentEventMapper;
private ShipmentAuditPublisher shipmentAuditPublisher;

@Override
public ShipmentResponse createShipment(
        CreateShipmentRequest request
) {
    validateShipmentCreation(request);

    String shipmentNumber =
            generateShipmentNumber();

    Shipment shipment =
            shipmentMapper.toEntity(
                    request,
                    shipmentNumber
            );

    Shipment savedShipment =
            shipmentRepository.save(shipment);

    createShipmentEvent(
            savedShipment,
            ShipmentEventType.CREATED,
            "Shipment created",
            savedShipment.getOriginLocation()
    );

    publishShipmentCreated(savedShipment);

    return shipmentMapper.toResponse(
            savedShipment
    );
}

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long shipmentId) {
        Shipment shipment = findShipmentById(shipmentId);
        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByNumber(String shipmentNumber) {
        Shipment shipment = shipmentRepository.findByShipmentNumber(shipmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with shipment number: " + shipmentNumber
                ));

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll()
                .stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByClientUserId(Long clientUserId) {
        return shipmentRepository.findByClientUserId(clientUserId)
                .stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Override
    public CargoDetailResponse addCargoDetail(
            Long shipmentId,
            CargoDetailRequest request
    ) {
        Shipment shipment = findShipmentById(shipmentId);

        validateCargoAddition(shipment);

        CargoDetail cargoDetail = cargoDetailMapper.toEntity(request, shipment);
        CargoDetail savedCargoDetail = cargoDetailRepository.save(cargoDetail);

        createShipmentEvent(
                shipment,
                ShipmentEventType.CARGO_ADDED,
                "Cargo added: " + savedCargoDetail.getCargoName(),
                shipment.getOriginLocation()
        );

        return cargoDetailMapper.toResponse(savedCargoDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDetailResponse> getCargoDetailsByShipmentId(Long shipmentId) {
        findShipmentById(shipmentId);

        return cargoDetailRepository.findByShipment_Id(shipmentId)
                .stream()
                .map(cargoDetailMapper::toResponse)
                .toList();
    }

@Override
public ShipmentResponse updateShipmentStatus(
        Long shipmentId,
        UpdateShipmentStatusRequest request
) {
    Shipment shipment =
            findShipmentById(shipmentId);

    ShipmentStatus previousStatus =
            shipment.getStatus();

    validateStatusTransition(
            previousStatus,
            request.getStatus()
    );

    shipment.setStatus(
            request.getStatus()
    );

    Shipment savedShipment =
            shipmentRepository.save(shipment);

    createShipmentEvent(
            savedShipment,
            mapStatusToEventType(
                    request.getStatus()
            ),
            "Shipment status updated to "
                    + request.getStatus(),
            null
    );

    publishShipmentStatusUpdated(
            savedShipment,
            previousStatus
    );

    return shipmentMapper.toResponse(
            savedShipment
    );
}
    @Override
    @Transactional(readOnly = true)
    public List<ShipmentEventResponse> getShipmentEventsByShipmentId(
            Long shipmentId
    ) {
        findShipmentById(shipmentId);

        return shipmentEventRepository
                .findByShipment_IdOrderByEventTimeDesc(shipmentId)
                .stream()
                .map(shipmentEventMapper::toResponse)
                .toList();
    }

@Autowired(required = false)
void setShipmentAuditPublisher(
        ShipmentAuditPublisher shipmentAuditPublisher
) {
    this.shipmentAuditPublisher =
            shipmentAuditPublisher;
}

    private Shipment findShipmentById(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Shipment not found with id: " + shipmentId
                ));
    }

    private void validateShipmentCreation(CreateShipmentRequest request) {
        if (request.getOriginLocation()
                .trim()
                .equalsIgnoreCase(request.getDestinationLocation().trim())) {

            throw new InvalidShipmentOperationException(
                    "Origin and destination locations cannot be the same"
            );
        }

        if (request.getExpectedPickupDate() != null
                && request.getExpectedDeliveryDate() != null
                && request.getExpectedDeliveryDate()
                .isBefore(request.getExpectedPickupDate())) {

            throw new InvalidShipmentOperationException(
                    "Expected delivery date cannot be before expected pickup date"
            );
        }
    }

    private void validateCargoAddition(Shipment shipment) {
        ShipmentStatus currentStatus = shipment.getStatus();

        if (currentStatus != ShipmentStatus.CREATED
                && currentStatus != ShipmentStatus.BOOKED) {

            throw new InvalidShipmentOperationException(
                    "Cargo cannot be added when shipment status is "
                            + currentStatus
            );
        }
    }

    private void validateStatusTransition(
            ShipmentStatus currentStatus,
            ShipmentStatus requestedStatus
    ) {
        if (requestedStatus == null) {
            throw new InvalidShipmentOperationException(
                    "Shipment status is required"
            );
        }

        if (currentStatus == requestedStatus) {
            throw new InvalidShipmentOperationException(
                    "Shipment is already in status " + currentStatus
            );
        }

        boolean validTransition = switch (currentStatus) {
            case CREATED ->
                    requestedStatus == ShipmentStatus.BOOKED
                            || requestedStatus == ShipmentStatus.CANCELLED;

            case BOOKED ->
                    requestedStatus == ShipmentStatus.IN_TRANSIT
                            || requestedStatus == ShipmentStatus.CANCELLED;

            case IN_TRANSIT ->
                    requestedStatus == ShipmentStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new InvalidShipmentOperationException(
                    "Invalid shipment status transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }
    }

private void publishShipmentCreated(
        Shipment shipment
) {
    if (shipmentAuditPublisher == null) {
        return;
    }

    shipmentAuditPublisher
            .publishShipmentCreated(shipment);
}

private void publishShipmentStatusUpdated(
        Shipment shipment,
        ShipmentStatus previousStatus
) {
    if (shipmentAuditPublisher == null) {
        return;
    }

    shipmentAuditPublisher
            .publishShipmentStatusUpdated(
                    shipment,
                    previousStatus
            );
}

    private void createShipmentEvent(
            Shipment shipment,
            ShipmentEventType eventType,
            String eventDescription,
            String eventLocation
    ) {
        ShipmentEvent shipmentEvent = shipmentEventMapper.toEntity(
                shipment,
                eventType,
                eventDescription,
                eventLocation
        );

        shipmentEventRepository.save(shipmentEvent);
    }

    private ShipmentEventType mapStatusToEventType(ShipmentStatus status) {
        return switch (status) {
            case CREATED -> ShipmentEventType.CREATED;
            case BOOKED -> ShipmentEventType.BOOKED;
            case IN_TRANSIT -> ShipmentEventType.IN_TRANSIT;
            case DELIVERED -> ShipmentEventType.DELIVERED;
            case CANCELLED -> ShipmentEventType.CANCELLED;
        };
    }

    private String generateShipmentNumber() {
        String datePart = LocalDate.now()
                .toString()
                .replace("-", "");

        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        String shipmentNumber =
                "CS-" + datePart + "-" + randomPart;

        while (shipmentRepository.existsByShipmentNumber(shipmentNumber)) {
            randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

            shipmentNumber =
                    "CS-" + datePart + "-" + randomPart;
        }

        return shipmentNumber;
    }
}