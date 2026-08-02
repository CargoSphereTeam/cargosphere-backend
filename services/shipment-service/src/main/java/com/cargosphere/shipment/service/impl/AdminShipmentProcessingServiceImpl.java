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
import com.cargosphere.shipment.service.AdminShipmentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminShipmentProcessingServiceImpl
        implements AdminShipmentProcessingService {

    private final ShipmentRepository shipmentRepository;

    private final ShipmentEventRepository shipmentEventRepository;

    private final ShipmentEventMapper shipmentEventMapper;

    private final ShipmentAuditPublisher shipmentAuditPublisher;

    @Override
    public ProcessingStartResponse startProcessing(
            Long shipmentId
    ) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shipment not found with id: "
                                        + shipmentId
                        )
                );

        validateCurrentStage(shipment);

        shipment.setProcessingStage(
                ProcessingStage.CONTAINER_ALLOCATION
        );

        shipment.setProcessingStartedAt(
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        Shipment savedShipment =
                shipmentRepository.save(shipment);

        createProcessingStartedEvent(savedShipment);

        shipmentAuditPublisher
                .publishAdminProcessingStarted(savedShipment);

        return ProcessingStartResponse.builder()
                .shipmentId(savedShipment.getId())
                .shipmentNumber(
                        savedShipment.getShipmentNumber()
                )
                .processingStage(
                        savedShipment.getProcessingStage()
                )
                .processingStartedAt(
                        savedShipment.getProcessingStartedAt()
                )
                .build();
    }

    private void validateCurrentStage(
            Shipment shipment
    ) {
        if (shipment.getProcessingStage()
                != ProcessingStage.PENDING_ADMIN_REVIEW) {

            throw new InvalidProcessingStageException(
                    shipment.getId(),
                    ProcessingStage.PENDING_ADMIN_REVIEW,
                    shipment.getProcessingStage()
            );
        }
    }

    private void createProcessingStartedEvent(
            Shipment shipment
    ) {
        boolean eventAlreadyExists =
                shipmentEventRepository
                        .existsByShipment_IdAndEventType(
                                shipment.getId(),
                                ShipmentEventType
                                        .ADMIN_PROCESSING_STARTED
                        );

        if (eventAlreadyExists) {
            return;
        }

        ShipmentEvent event =
                shipmentEventMapper.toEntity(
                        shipment,
                        ShipmentEventType
                                .ADMIN_PROCESSING_STARTED,
                        "Administrative shipment processing started",
                        shipment.getOriginLocation()
                );

        shipmentEventRepository.save(event);
    }
}