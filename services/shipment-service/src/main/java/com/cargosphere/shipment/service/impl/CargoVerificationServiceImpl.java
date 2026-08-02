package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.audit.CurrentActor;
import com.cargosphere.shipment.audit.ShipmentActorProvider;
import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import com.cargosphere.shipment.dto.admin.CargoVerificationAction;
import com.cargosphere.shipment.dto.admin.CargoVerificationItemRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationItemResponse;
import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.exception.InvalidProcessingStageException;
import com.cargosphere.shipment.exception.InvalidShipmentOperationException;
import com.cargosphere.shipment.exception.ResourceNotFoundException;
import com.cargosphere.shipment.mapper.CargoVerificationMapper;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.repository.CargoDetailRepository;
import com.cargosphere.shipment.repository.CargoVerificationRepository;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import com.cargosphere.shipment.service.CargoVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CargoVerificationServiceImpl
        implements CargoVerificationService {

    private final ShipmentRepository shipmentRepository;

    private final CargoDetailRepository cargoDetailRepository;

    private final CargoVerificationRepository
            cargoVerificationRepository;

    private final ShipmentEventRepository shipmentEventRepository;

    private final CargoVerificationMapper cargoVerificationMapper;

    private final ShipmentEventMapper shipmentEventMapper;

    private final ShipmentActorProvider shipmentActorProvider;

    private final ShipmentAuditPublisher shipmentAuditPublisher;

    @Override
    public CargoVerificationResponse saveOrConfirm(
            Long shipmentId,
            CargoVerificationRequest request
    ) {
        Shipment shipment = findShipmentById(shipmentId);

        validateProcessingStage(shipment);

        validateRequest(request);

        List<CargoDetail> cargoDetails =
                cargoDetailRepository.findByShipment_Id(
                        shipmentId
                );

        if (cargoDetails.isEmpty()) {
            throw new InvalidShipmentOperationException(
                    "Shipment "
                            + shipmentId
                            + " has no cargo details to verify"
            );
        }

        Map<Long, CargoDetail> cargoDetailsById =
                createCargoDetailsByIdMap(cargoDetails);

        validateRequestedCargoDetails(
                shipmentId,
                request.getItems(),
                cargoDetailsById
        );

        upsertVerificationItems(
                request,
                cargoDetailsById
        );

        if (request.getAction()
                == CargoVerificationAction.CONFIRM_AND_CONTINUE) {

            return confirmAndContinue(
                    shipment,
                    request.getAction(),
                    cargoDetails
            );
        }

        return buildResponse(
                shipment,
                request.getAction()
        );
    }

    private Shipment findShipmentById(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shipment not found with id: "
                                        + shipmentId
                        )
                );
    }

    private void validateProcessingStage(
            Shipment shipment
    ) {
        if (shipment.getProcessingStage()
                != ProcessingStage.CARGO_VERIFICATION) {

            throw new InvalidProcessingStageException(
                    shipment.getId(),
                    ProcessingStage.CARGO_VERIFICATION,
                    shipment.getProcessingStage()
            );
        }
    }

    private void validateRequest(
            CargoVerificationRequest request
    ) {
        if (request == null) {
            throw new InvalidShipmentOperationException(
                    "Cargo verification request is required"
            );
        }

        if (request.getAction() == null) {
            throw new InvalidShipmentOperationException(
                    "Cargo verification action is required"
            );
        }

        if (request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new InvalidShipmentOperationException(
                    "At least one cargo verification item is required"
            );
        }
    }

    private Map<Long, CargoDetail> createCargoDetailsByIdMap(
            List<CargoDetail> cargoDetails
    ) {
        Map<Long, CargoDetail> cargoDetailsById =
                new HashMap<>();

        for (CargoDetail cargoDetail : cargoDetails) {
            cargoDetailsById.put(
                    cargoDetail.getId(),
                    cargoDetail
            );
        }

        return cargoDetailsById;
    }

    private void validateRequestedCargoDetails(
            Long shipmentId,
            List<CargoVerificationItemRequest> items,
            Map<Long, CargoDetail> cargoDetailsById
    ) {
        Set<Long> requestedCargoDetailIds =
                new HashSet<>();

        for (CargoVerificationItemRequest item : items) {
            if (item == null
                    || item.getCargoDetailId() == null) {

                throw new InvalidShipmentOperationException(
                        "Cargo detail ID is required"
                );
            }

            Long cargoDetailId =
                    item.getCargoDetailId();

            if (!requestedCargoDetailIds.add(
                    cargoDetailId
            )) {
                throw new InvalidShipmentOperationException(
                        "Cargo detail ID "
                                + cargoDetailId
                                + " is duplicated in the request"
                );
            }

            if (!cargoDetailsById.containsKey(
                    cargoDetailId
            )) {
                throw new InvalidShipmentOperationException(
                        "Cargo detail "
                                + cargoDetailId
                                + " does not belong to shipment "
                                + shipmentId
                );
            }
        }
    }

    private void upsertVerificationItems(
            CargoVerificationRequest request,
            Map<Long, CargoDetail> cargoDetailsById
    ) {
        List<Long> requestedCargoDetailIds =
                request.getItems()
                        .stream()
                        .map(
                                CargoVerificationItemRequest
                                        ::getCargoDetailId
                        )
                        .toList();

        Map<Long, CargoVerification>
                existingVerificationsByCargoId =
                new HashMap<>();

        cargoVerificationRepository
                .findByCargoDetail_IdIn(
                        requestedCargoDetailIds
                )
                .forEach(verification ->
                        existingVerificationsByCargoId.put(
                                verification
                                        .getCargoDetail()
                                        .getId(),
                                verification
                        )
                );

        List<CargoVerification> verifications =
                request.getItems()
                        .stream()
                        .map(item -> createOrUpdateVerification(
                                item,
                                cargoDetailsById,
                                existingVerificationsByCargoId,
                                request.getAction()
                        ))
                        .toList();

        cargoVerificationRepository.saveAll(
                verifications
        );
    }

    private CargoVerification createOrUpdateVerification(
            CargoVerificationItemRequest item,
            Map<Long, CargoDetail> cargoDetailsById,
            Map<Long, CargoVerification>
                    existingVerificationsByCargoId,
            CargoVerificationAction action
    ) {
        Long cargoDetailId =
                item.getCargoDetailId();

        CargoVerification verification =
                existingVerificationsByCargoId.get(
                        cargoDetailId
                );

        if (verification == null) {
            verification =
                    cargoVerificationMapper.toNewEntity(
                            cargoDetailsById.get(
                                    cargoDetailId
                            ),
                            item
                    );
        } else {
            cargoVerificationMapper.updateEntity(
                    verification,
                    item
            );
        }

        if (action
                == CargoVerificationAction.SAVE_DRAFT) {

            verification.setVerificationStatus(
                    CargoVerificationStatus.DRAFT
            );

            verification.setVerifiedBy(null);
            verification.setVerifiedAt(null);
        }

        return verification;
    }

    private CargoVerificationResponse confirmAndContinue(
            Shipment shipment,
            CargoVerificationAction action,
            List<CargoDetail> cargoDetails
    ) {
        List<CargoVerification> verifications =
                cargoVerificationRepository
                        .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                                shipment.getId()
                        );

        validateAllCargoItemsHaveVerification(
                shipment.getId(),
                cargoDetails,
                verifications
        );

        CurrentActor actor =
                shipmentActorProvider.getCurrentActor();

        if (actor == null
                || actor.userId() == null) {

            throw new InvalidShipmentOperationException(
                    "Authenticated administrator user ID is required "
                            + "to confirm cargo verification"
            );
        }

        LocalDateTime verifiedAt =
                LocalDateTime.now();

        for (CargoVerification verification
                : verifications) {

            validateConfirmedValues(verification);

            verification.setVerificationStatus(
                    CargoVerificationStatus.CONFIRMED
            );

            verification.setVerifiedBy(
                    actor.userId()
            );

            verification.setVerifiedAt(
                    verifiedAt
            );
        }

        cargoVerificationRepository.saveAll(
                verifications
        );

        shipment.setProcessingStage(
                ProcessingStage.DOCUMENT_VERIFICATION
        );

        Shipment savedShipment =
                shipmentRepository.save(shipment);

        createCargoVerifiedEvent(savedShipment);

        shipmentAuditPublisher.publishCargoVerified(
                savedShipment
        );

        return buildResponse(
                savedShipment,
                action
        );
    }

    private void validateAllCargoItemsHaveVerification(
            Long shipmentId,
            List<CargoDetail> cargoDetails,
            List<CargoVerification> verifications
    ) {
        Set<Long> verifiedCargoDetailIds =
                new HashSet<>();

        for (CargoVerification verification
                : verifications) {

            verifiedCargoDetailIds.add(
                    verification
                            .getCargoDetail()
                            .getId()
            );
        }

        for (CargoDetail cargoDetail
                : cargoDetails) {

            if (!verifiedCargoDetailIds.contains(
                    cargoDetail.getId()
            )) {
                throw new InvalidShipmentOperationException(
                        "Cargo detail "
                                + cargoDetail.getId()
                                + " must be verified before continuing "
                                + "shipment "
                                + shipmentId
                );
            }
        }
    }

    private void validateConfirmedValues(
            CargoVerification verification
    ) {
        Long cargoDetailId =
                verification
                        .getCargoDetail()
                        .getId();

        if (verification.getConfirmedCargoName() == null
                || verification
                .getConfirmedCargoName()
                .isBlank()) {

            throw missingConfirmedValue(
                    cargoDetailId,
                    "cargo name"
            );
        }

        if (verification.getConfirmedWeightKg() == null
                || verification
                .getConfirmedWeightKg()
                .signum() <= 0) {

            throw missingConfirmedValue(
                    cargoDetailId,
                    "weight"
            );
        }

        if (verification.getConfirmedQuantity() == null
                || verification
                .getConfirmedQuantity() <= 0) {

            throw missingConfirmedValue(
                    cargoDetailId,
                    "quantity"
            );
        }

        if (verification.getConfirmedFragile() == null) {
            throw missingConfirmedValue(
                    cargoDetailId,
                    "fragile indicator"
            );
        }

        if (verification.getConfirmedHazardous() == null) {
            throw missingConfirmedValue(
                    cargoDetailId,
                    "hazardous indicator"
            );
        }

        if (verification.getConfirmedVolumeCbm() != null
                && verification
                .getConfirmedVolumeCbm()
                .signum() <= 0) {

            throw new InvalidShipmentOperationException(
                    "Confirmed volume must be greater than zero "
                            + "for cargo detail "
                            + cargoDetailId
            );
        }
    }

    private InvalidShipmentOperationException
    missingConfirmedValue(
            Long cargoDetailId,
            String field
    ) {
        return new InvalidShipmentOperationException(
                "Confirmed "
                        + field
                        + " is required for cargo detail "
                        + cargoDetailId
        );
    }

    private void createCargoVerifiedEvent(
            Shipment shipment
    ) {
        boolean eventAlreadyExists =
                shipmentEventRepository
                        .existsByShipment_IdAndEventType(
                                shipment.getId(),
                                ShipmentEventType.CARGO_VERIFIED
                        );

        if (eventAlreadyExists) {
            return;
        }

        ShipmentEvent event =
                shipmentEventMapper.toEntity(
                        shipment,
                        ShipmentEventType.CARGO_VERIFIED,
                        "Shipment cargo verification confirmed",
                        shipment.getOriginLocation()
                );

        shipmentEventRepository.save(event);
    }

    private CargoVerificationResponse buildResponse(
            Shipment shipment,
            CargoVerificationAction action
    ) {
        List<CargoVerificationItemResponse> items =
                cargoVerificationRepository
                        .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                                shipment.getId()
                        )
                        .stream()
                        .map(
                                cargoVerificationMapper
                                        ::toResponse
                        )
                        .toList();

        return CargoVerificationResponse.builder()
                .shipmentId(shipment.getId())
                .action(action)
                .processingStage(
                        shipment.getProcessingStage()
                )
                .items(items)
                .build();
    }
}