package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    @Mock
    private CargoDetailRepository cargoDetailRepository;

    @Mock
    private CargoVerificationRepository
            cargoVerificationRepository;

    @Mock
    private ContainerAllocationClient
            containerAllocationClient;

    @Mock
    private ShipmentDocumentClient
            shipmentDocumentClient;

    @Mock
    private ShipmentPaymentClient
            shipmentPaymentClient;

    private AdminShipmentProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminShipmentProcessingServiceImpl(
                shipmentRepository,
                shipmentEventRepository,
                new ShipmentEventMapper(),
                shipmentAuditPublisher,
                cargoDetailRepository,
                cargoVerificationRepository,
                containerAllocationClient,
                shipmentDocumentClient,
                shipmentPaymentClient
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
                        any(ShipmentEvent.class)
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
                        any(Shipment.class)
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
                        any(ShipmentEvent.class)
                );

        verify(shipmentAuditPublisher)
                .publishAdminProcessingStarted(shipment);
    }

    @Test
    void shouldReturnPaginatedProcessingQueueOldestFirst() {
        Shipment firstShipment = Shipment.builder()
                .id(10L)
                .shipmentNumber("SHP-2026-00010")
                .clientUserId(100L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .processingStage(
                        ProcessingStage.PENDING_ADMIN_REVIEW
                )
                .createdAt(
                        OffsetDateTime.parse(
                                "2026-08-01T08:00:00Z"
                        )
                )
                .updatedAt(
                        OffsetDateTime.parse(
                                "2026-08-01T08:00:00Z"
                        )
                )
                .build();

        Shipment secondShipment = Shipment.builder()
                .id(11L)
                .shipmentNumber("SHP-2026-00011")
                .clientUserId(101L)
                .originLocation("Delhi")
                .destinationLocation("Jaipur")
                .processingStage(
                        ProcessingStage.CONTAINER_ALLOCATION
                )
                .createdAt(
                        OffsetDateTime.parse(
                                "2026-08-01T09:00:00Z"
                        )
                )
                .updatedAt(
                        OffsetDateTime.parse(
                                "2026-08-01T09:00:00Z"
                        )
                )
                .build();

        Page<Shipment> shipmentPage =
                new PageImpl<>(
                        List.of(
                                firstShipment,
                                secondShipment
                        ),
                        PageRequest.of(0, 20),
                        2
                );

        when(shipmentRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(shipmentPage);

        ProcessingQueueResponse response =
                service.getProcessingQueue(
                        null,
                        0,
                        20
                );

        assertThat(response.getItems())
                .hasSize(2);

        assertThat(response.getItems().get(0)
                .getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getItems().get(1)
                .getShipmentId())
                .isEqualTo(11L);

        assertThat(response.getPage())
                .isZero();

        assertThat(response.getSize())
                .isEqualTo(20);

        assertThat(response.getTotalElements())
                .isEqualTo(2);

        assertThat(response.getTotalPages())
                .isEqualTo(1);

        assertThat(response.isFirst())
                .isTrue();

        assertThat(response.isLast())
                .isTrue();

        assertThat(response.isEmpty())
                .isFalse();

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(
                        Pageable.class
                );

        verify(shipmentRepository).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        Pageable pageable =
                pageableCaptor.getValue();

        assertThat(pageable.getPageNumber())
                .isZero();

        assertThat(pageable.getPageSize())
                .isEqualTo(20);

        Sort.Order createdAtOrder =
                pageable.getSort()
                        .getOrderFor("createdAt");

        Sort.Order idOrder =
                pageable.getSort()
                        .getOrderFor("id");

        assertThat(createdAtOrder)
                .isNotNull();

        assertThat(createdAtOrder.getDirection())
                .isEqualTo(Sort.Direction.ASC);

        assertThat(idOrder)
                .isNotNull();

        assertThat(idOrder.getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterProcessingQueueByStage() {
        Page<Shipment> emptyPage =
                Page.empty(
                        PageRequest.of(0, 20)
                );

        when(shipmentRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        service.getProcessingQueue(
                ProcessingStage.CARGO_VERIFICATION,
                0,
                20
        );

        ArgumentCaptor<Specification<Shipment>>
                specificationCaptor =
                ArgumentCaptor.forClass(
                        Specification.class
                );

        verify(shipmentRepository).findAll(
                specificationCaptor.capture(),
                any(Pageable.class)
        );

        Root<Shipment> root =
                mock(Root.class);

        CriteriaQuery<?> query =
                mock(CriteriaQuery.class);

        CriteriaBuilder criteriaBuilder =
                mock(CriteriaBuilder.class);

        Path<ProcessingStage> stagePath =
                mock(Path.class);

        Predicate expectedPredicate =
                mock(Predicate.class);

        when(root.<ProcessingStage>get(
                "processingStage"
        )).thenReturn(stagePath);

        when(criteriaBuilder.equal(
                stagePath,
                ProcessingStage.CARGO_VERIFICATION
        )).thenReturn(expectedPredicate);

        Predicate actualPredicate =
                specificationCaptor
                        .getValue()
                        .toPredicate(
                                root,
                                query,
                                criteriaBuilder
                        );

        assertThat(actualPredicate)
                .isSameAs(expectedPredicate);

        verify(criteriaBuilder).equal(
                stagePath,
                ProcessingStage.CARGO_VERIFICATION
        );
    }

    @Test
    void shouldRejectNegativeProcessingQueuePage() {
        assertThatThrownBy(() ->
                service.getProcessingQueue(
                        null,
                        -1,
                        20
                )
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessageContaining(
                        "Page number must be zero or greater"
                );

        verify(shipmentRepository, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldRejectProcessingQueueSizeAboveMaximum() {
        assertThatThrownBy(() ->
                service.getProcessingQueue(
                        null,
                        0,
                        101
                )
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessageContaining(
                        "Page size must be between 1 and 100"
                );

        verify(shipmentRepository, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldReportShipmentReadyForEbill() {
        Shipment shipment = createShipment(
                ProcessingStage.READY_FOR_EBILL
        );

        CargoDetail cargoDetail = CargoDetail.builder()
                .id(20L)
                .shipment(shipment)
                .cargoName("Electronics")
                .build();

        CargoVerification verification =
                CargoVerification.builder()
                        .id(30L)
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.CONFIRMED
                        )
                        .verifiedBy(5L)
                        .verifiedAt(
                                LocalDateTime.of(
                                        2026,
                                        8,
                                        2,
                                        10,
                                        0
                                )
                        )
                        .build();

        ContainerAllocationResponse allocation =
                new ContainerAllocationResponse(
                        1L,
                        10L,
                        2L,
                        "DRY_20",
                        "20 Foot Dry Container",
                        1,
                        "ALLOCATED",
                        null,
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                9,
                                0
                        )
                );

        ShipmentDocumentResponse document =
                new ShipmentDocumentResponse(
                        1L,
                        10L,
                        "COMMERCIAL_INVOICE",
                        true,
                        "VERIFIED",
                        5L,
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                10,
                                0
                        ),
                        "Verified",
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                10,
                                0
                        )
                );

        ShipmentPaymentResponse payment =
                new ShipmentPaymentResponse(
                        1L,
                        10L,
                        100L,
                        new BigDecimal("12500.00"),
                        "INR",
                        "UPI",
                        "PAID",
                        "FULL",
                        "TXN-2026-000123",
                        LocalDate.of(2026, 8, 15),
                        LocalDate.of(2026, 8, 2),
                        "Payment confirmed",
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                2,
                                10,
                                0
                        )
                );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(containerAllocationClient
                .getAllocationsByShipmentId(10L))
                .thenReturn(List.of(allocation));

        when(cargoDetailRepository
                .findByShipment_Id(10L))
                .thenReturn(List.of(cargoDetail));

        when(cargoVerificationRepository
                .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                        10L
                ))
                .thenReturn(List.of(verification));

        when(shipmentDocumentClient
                .getDocumentsByShipmentId(10L))
                .thenReturn(List.of(document));

        when(shipmentPaymentClient
                .getPaymentsByShipmentId(10L))
                .thenReturn(List.of(payment));

        ProcessingReadinessResponse response =
                service.getProcessingReadiness(10L);

        assertThat(response.getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.READY_FOR_EBILL
                );

        assertThat(response.isContainerReady())
                .isTrue();

        assertThat(response.isCargoReady())
                .isTrue();

        assertThat(response.isDocumentsReady())
                .isTrue();

        assertThat(response.isPaymentReady())
                .isTrue();

        assertThat(response.isEbillReady())
                .isTrue();

        assertThat(response.getBlockingReasons())
                .isEmpty();
    }
    @Test
    void shouldReportBlockingReasonsWhenRequirementsAreIncomplete() {
        Shipment shipment = createShipment(
                ProcessingStage.READY_FOR_EBILL
        );

        CargoDetail cargoDetail = CargoDetail.builder()
                .id(20L)
                .shipment(shipment)
                .cargoName("Electronics")
                .build();

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(containerAllocationClient
                .getAllocationsByShipmentId(10L))
                .thenReturn(List.of());

        when(cargoDetailRepository
                .findByShipment_Id(10L))
                .thenReturn(List.of(cargoDetail));

        when(cargoVerificationRepository
                .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                        10L
                ))
                .thenReturn(List.of());

        when(shipmentDocumentClient
                .getDocumentsByShipmentId(10L))
                .thenReturn(List.of());

        when(shipmentPaymentClient
                .getPaymentsByShipmentId(10L))
                .thenReturn(List.of());

        ProcessingReadinessResponse response =
                service.getProcessingReadiness(10L);

        assertThat(response.isContainerReady())
                .isFalse();

        assertThat(response.isCargoReady())
                .isFalse();

        assertThat(response.isDocumentsReady())
                .isFalse();

        assertThat(response.isPaymentReady())
                .isFalse();

        assertThat(response.isEbillReady())
                .isFalse();

        assertThat(response.getBlockingReasons())
                .containsExactly(
                        "No valid container allocation exists",
                        "All shipment cargo items must be confirmed",
                        "All required shipment documents must be verified",
                        "At least one valid PAID payment is required"
                );
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
                        any(ShipmentEvent.class)
                );

        verify(shipmentAuditPublisher, never())
                .publishAdminProcessingStarted(
                        any(Shipment.class)
                );
    }
}