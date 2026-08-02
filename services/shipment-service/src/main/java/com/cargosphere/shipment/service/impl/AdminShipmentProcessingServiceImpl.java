package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import com.cargosphere.shipment.dto.admin.ProcessingContinueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueItemResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
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
import com.cargosphere.shipment.integration.container.ContainerAllocationClient;
import com.cargosphere.shipment.integration.container.ContainerAllocationResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentClient;
import com.cargosphere.shipment.integration.document.ShipmentDocumentResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentClient;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentResponse;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.repository.CargoDetailRepository;
import com.cargosphere.shipment.repository.CargoVerificationRepository;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import com.cargosphere.shipment.service.AdminShipmentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminShipmentProcessingServiceImpl
        implements AdminShipmentProcessingService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ShipmentRepository shipmentRepository;

    private final ShipmentEventRepository shipmentEventRepository;

    private final ShipmentEventMapper shipmentEventMapper;

    private final ShipmentAuditPublisher shipmentAuditPublisher;

    private final CargoDetailRepository cargoDetailRepository;

    private final CargoVerificationRepository
            cargoVerificationRepository;

    private final ContainerAllocationClient
            containerAllocationClient;

    private final ShipmentDocumentClient
            shipmentDocumentClient;

    private final ShipmentPaymentClient
            shipmentPaymentClient;

    @Override
    public ProcessingStartResponse startProcessing(
            Long shipmentId
    ) {
        Shipment shipment = findShipmentById(shipmentId);

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

    @Override
    public ProcessingContinueResponse continueProcessing(
            Long shipmentId
    ) {
        Shipment shipment = findShipmentById(shipmentId);

        ProcessingStage previousStage =
                shipment.getProcessingStage();

        ProcessingStage nextStage =
                determineNextProcessingStage(
                        shipmentId,
                        previousStage
                );

        shipment.setProcessingStage(nextStage);

        Shipment savedShipment =
                shipmentRepository.save(shipment);

        createProcessingAdvancedEvent(
                savedShipment,
                nextStage
        );

        shipmentAuditPublisher.publishProcessingAdvanced(
                savedShipment,
                previousStage
        );

        return ProcessingContinueResponse.builder()
                .shipmentId(savedShipment.getId())
                .shipmentNumber(
                        savedShipment.getShipmentNumber()
                )
                .previousStage(previousStage)
                .processingStage(nextStage)
                .advancedAt(
                        OffsetDateTime.now(ZoneOffset.UTC)
                )
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public ProcessingQueueResponse getProcessingQueue(
            ProcessingStage processingStage,
            int page,
            int size
    ) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("createdAt"),
                        Sort.Order.asc("id")
                )
        );

        Specification<Shipment> specification =
                createQueueSpecification(processingStage);

        Page<Shipment> shipmentPage =
                shipmentRepository.findAll(
                        specification,
                        pageable
                );

        List<ProcessingQueueItemResponse> items =
                shipmentPage.getContent()
                        .stream()
                        .map(this::toQueueItemResponse)
                        .toList();

        return ProcessingQueueResponse.builder()
                .items(items)
                .page(shipmentPage.getNumber())
                .size(shipmentPage.getSize())
                .totalElements(
                        shipmentPage.getTotalElements()
                )
                .totalPages(
                        shipmentPage.getTotalPages()
                )
                .first(shipmentPage.isFirst())
                .last(shipmentPage.isLast())
                .empty(shipmentPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessingReadinessResponse getProcessingReadiness(
            Long shipmentId
    ) {
        Shipment shipment = findShipmentById(shipmentId);

        List<ContainerAllocationResponse> allocations =
                containerAllocationClient
                        .getAllocationsByShipmentId(shipmentId);

        List<CargoDetail> cargoDetails =
                cargoDetailRepository
                        .findByShipment_Id(shipmentId);

        List<CargoVerification> cargoVerifications =
                cargoVerificationRepository
                        .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                                shipmentId
                        );

        List<ShipmentDocumentResponse> documents =
                shipmentDocumentClient
                        .getDocumentsByShipmentId(shipmentId);

        List<ShipmentPaymentResponse> payments =
                shipmentPaymentClient
                        .getPaymentsByShipmentId(shipmentId);

        boolean containerReady =
                isContainerReady(
                        shipmentId,
                        allocations
                );

        boolean cargoReady =
                isCargoReady(
                        cargoDetails,
                        cargoVerifications
                );

        boolean documentsReady =
                areDocumentsReady(
                        shipmentId,
                        documents
                );

        boolean paymentReady =
                isPaymentReady(
                        shipmentId,
                        payments
                );

        boolean processingStageReady =
                shipment.getProcessingStage()
                        == ProcessingStage.READY_FOR_EBILL;

        boolean ebillReady =
                containerReady
                        && cargoReady
                        && documentsReady
                        && paymentReady
                        && processingStageReady;

        List<String> blockingReasons =
                buildBlockingReasons(
                        containerReady,
                        cargoReady,
                        documentsReady,
                        paymentReady,
                        processingStageReady
                );

        return ProcessingReadinessResponse.builder()
                .shipmentId(shipment.getId())
                .shipmentNumber(
                        shipment.getShipmentNumber()
                )
                .processingStage(
                        shipment.getProcessingStage()
                )
                .containerReady(containerReady)
                .cargoReady(cargoReady)
                .documentsReady(documentsReady)
                .paymentReady(paymentReady)
                .ebillReady(ebillReady)
                .blockingReasons(blockingReasons)
                .build();
    }

    private boolean isContainerReady(
            Long shipmentId,
            List<ContainerAllocationResponse> allocations
    ) {
        return allocations.stream()
                .anyMatch(allocation ->
                        allocation != null
                                && allocation.allocationId()
                                != null
                                && Objects.equals(
                                allocation.shipmentId(),
                                shipmentId
                        )
                                && allocation.containerTypeId()
                                != null
                                && allocation.quantity()
                                != null
                                && allocation.quantity() >= 1
                );
    }

    private boolean isCargoReady(
            List<CargoDetail> cargoDetails,
            List<CargoVerification> verifications
    ) {
        if (cargoDetails.isEmpty()) {
            return false;
        }

        return cargoDetails.stream()
                .allMatch(cargoDetail ->
                        hasConfirmedVerification(
                                cargoDetail,
                                verifications
                        )
                );
    }

    private boolean hasConfirmedVerification(
            CargoDetail cargoDetail,
            List<CargoVerification> verifications
    ) {
        return verifications.stream()
                .anyMatch(verification ->
                        verification != null
                                && verification
                                .getCargoDetail() != null
                                && Objects.equals(
                                verification
                                        .getCargoDetail()
                                        .getId(),
                                cargoDetail.getId()
                        )
                                && verification
                                .getVerificationStatus()
                                == CargoVerificationStatus.CONFIRMED
                                && verification.getVerifiedBy()
                                != null
                                && verification.getVerifiedAt()
                                != null
                );
    }

    private boolean areDocumentsReady(
            Long shipmentId,
            List<ShipmentDocumentResponse> documents
    ) {
        if (documents.isEmpty()) {
            return false;
        }

        return documents.stream()
                .allMatch(document -> {
                    if (document == null
                            || document.id() == null
                            || !Objects.equals(
                            document.shipmentId(),
                            shipmentId
                    )) {
                        return false;
                    }

                    if (!Boolean.TRUE.equals(
                            document.required()
                    )) {
                        return true;
                    }

                    return "VERIFIED".equalsIgnoreCase(
                            document.verificationStatus()
                    )
                            && document.verifiedBy() != null
                            && document.verifiedAt() != null;
                });
    }

    private boolean isPaymentReady(
            Long shipmentId,
            List<ShipmentPaymentResponse> payments
    ) {
        return payments.stream()
                .anyMatch(payment ->
                        payment != null
                                && payment.id() != null
                                && Objects.equals(
                                payment.shipmentId(),
                                shipmentId
                        )
                                && payment.amount() != null
                                && payment.amount()
                                .compareTo(BigDecimal.ZERO) > 0
                                && "PAID".equalsIgnoreCase(
                                payment.paymentStatus()
                        )
                                && payment.paidDate() != null
                );
    }

    private ProcessingStage determineNextProcessingStage(
            Long shipmentId,
            ProcessingStage currentStage
    ) {
        if (currentStage == null) {
            throw new InvalidShipmentOperationException(
                    "Shipment "
                            + shipmentId
                            + " has no processing stage"
            );
        }

        return switch (currentStage) {
            case CONTAINER_ALLOCATION ->
                    continueAfterContainerAllocation(
                            shipmentId
                    );

            case DOCUMENT_VERIFICATION ->
                    continueAfterDocumentVerification(
                            shipmentId
                    );

            case PAYMENT_CONFIRMATION ->
                    continueAfterPaymentConfirmation(
                            shipmentId
                    );

            case CARGO_VERIFICATION ->
                    throw new InvalidShipmentOperationException(
                            "Cargo verification must be confirmed "
                                    + "through the cargo verification endpoint"
                    );

            default ->
                    throw new InvalidShipmentOperationException(
                            "Shipment "
                                    + shipmentId
                                    + " cannot continue from processing stage "
                                    + currentStage
                    );
        };
    }

    private ProcessingStage continueAfterContainerAllocation(
            Long shipmentId
    ) {
        List<ContainerAllocationResponse> allocations =
                containerAllocationClient
                        .getAllocationsByShipmentId(
                                shipmentId
                        );

        if (!isContainerReady(
                shipmentId,
                allocations
        )) {
            throw new InvalidShipmentOperationException(
                    "A valid container allocation is required "
                            + "before continuing shipment "
                            + shipmentId
            );
        }

        return ProcessingStage.CARGO_VERIFICATION;
    }

    private ProcessingStage continueAfterDocumentVerification(
            Long shipmentId
    ) {
        List<ShipmentDocumentResponse> documents =
                shipmentDocumentClient
                        .getDocumentsByShipmentId(
                                shipmentId
                        );

        if (!areDocumentsReady(
                shipmentId,
                documents
        )) {
            throw new InvalidShipmentOperationException(
                    "All required shipment documents must be verified "
                            + "before continuing shipment "
                            + shipmentId
            );
        }

        return ProcessingStage.PAYMENT_CONFIRMATION;
    }

    private ProcessingStage continueAfterPaymentConfirmation(
            Long shipmentId
    ) {
        List<ShipmentPaymentResponse> payments =
                shipmentPaymentClient
                        .getPaymentsByShipmentId(
                                shipmentId
                        );

        if (!isPaymentReady(
                shipmentId,
                payments
        )) {
            throw new InvalidShipmentOperationException(
                    "At least one valid PAID payment is required "
                            + "before continuing shipment "
                            + shipmentId
            );
        }

        return ProcessingStage.READY_FOR_EBILL;
    }

    private void createProcessingAdvancedEvent(
            Shipment shipment,
            ProcessingStage nextStage
    ) {
        ShipmentEventType eventType =
                switch (nextStage) {
                    case CARGO_VERIFICATION ->
                            ShipmentEventType
                                    .CONTAINER_ALLOCATED;

                    case PAYMENT_CONFIRMATION ->
                            ShipmentEventType
                                    .DOCUMENTS_VERIFIED;

                    case READY_FOR_EBILL ->
                            ShipmentEventType
                                    .PAYMENT_CONFIRMED;

                    default ->
                            throw new InvalidShipmentOperationException(
                                    "Unsupported processing stage transition to "
                                            + nextStage
                            );
                };

        boolean eventAlreadyExists =
                shipmentEventRepository
                        .existsByShipment_IdAndEventType(
                                shipment.getId(),
                                eventType
                        );

        if (eventAlreadyExists) {
            return;
        }

        String description =
                switch (eventType) {
                    case CONTAINER_ALLOCATED ->
                            "Shipment container allocation confirmed";

                    case DOCUMENTS_VERIFIED ->
                            "Shipment document verification confirmed";

                    case PAYMENT_CONFIRMED ->
                            "Shipment payment confirmation completed";

                    default ->
                            throw new InvalidShipmentOperationException(
                                    "Unsupported shipment processing event "
                                            + eventType
                            );
                };

        ShipmentEvent event =
                shipmentEventMapper.toEntity(
                        shipment,
                        eventType,
                        description,
                        shipment.getOriginLocation()
                );

        shipmentEventRepository.save(event);
    }
    private List<String> buildBlockingReasons(
            boolean containerReady,
            boolean cargoReady,
            boolean documentsReady,
            boolean paymentReady,
            boolean processingStageReady
    ) {
        List<String> reasons = new ArrayList<>();

        if (!containerReady) {
            reasons.add(
                    "No valid container allocation exists"
            );
        }

        if (!cargoReady) {
            reasons.add(
                    "All shipment cargo items must be confirmed"
            );
        }

        if (!documentsReady) {
            reasons.add(
                    "All required shipment documents must be verified"
            );
        }

        if (!paymentReady) {
            reasons.add(
                    "At least one valid PAID payment is required"
            );
        }

        if (!processingStageReady) {
            reasons.add(
                    "Processing stage must be READY_FOR_EBILL"
            );
        }

        return List.copyOf(reasons);
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

    private void validatePagination(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new InvalidShipmentOperationException(
                    "Page number must be zero or greater"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidShipmentOperationException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }

    private Specification<Shipment>
    createQueueSpecification(
            ProcessingStage processingStage
    ) {
        return (root, query, criteriaBuilder) -> {
            if (processingStage == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("processingStage"),
                    processingStage
            );
        };
    }

    private ProcessingQueueItemResponse toQueueItemResponse(
            Shipment shipment
    ) {
        return ProcessingQueueItemResponse.builder()
                .shipmentId(shipment.getId())
                .shipmentNumber(
                        shipment.getShipmentNumber()
                )
                .clientUserId(
                        shipment.getClientUserId()
                )
                .originLocation(
                        shipment.getOriginLocation()
                )
                .destinationLocation(
                        shipment.getDestinationLocation()
                )
                .shipmentType(
                        shipment.getShipmentType()
                )
                .shipmentStatus(
                        shipment.getStatus()
                )
                .processingStage(
                        shipment.getProcessingStage()
                )
                .expectedPickupDate(
                        shipment.getExpectedPickupDate()
                )
                .expectedDeliveryDate(
                        shipment.getExpectedDeliveryDate()
                )
                .processingStartedAt(
                        shipment.getProcessingStartedAt()
                )
                .processingCompletedAt(
                        shipment.getProcessingCompletedAt()
                )
                .createdAt(
                        shipment.getCreatedAt()
                )
                .updatedAt(
                        shipment.getUpdatedAt()
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
