package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.service.support.EbillNumberGenerator;
import com.cargosphere.shipment.service.support.EbillSnapshotJsonService;
import com.cargosphere.shipment.service.support.EbillPdfGenerator;
import com.cargosphere.shipment.service.support.ProcessingReadinessEvaluator;

import com.cargosphere.shipment.audit.CurrentActor;
import com.cargosphere.shipment.audit.ShipmentActorProvider;
import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import com.cargosphere.shipment.dto.admin.ProcessingContinueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.dto.ebill.EbillGenerationResponse;
import com.cargosphere.shipment.dto.ebill.EbillPdfDocument;
import com.cargosphere.shipment.dto.ebill.EbillPreviewResponse;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillSnapshot;
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
import com.cargosphere.shipment.integration.auth.AuthUserClient;
import com.cargosphere.shipment.integration.auth.AuthUserResponse;
import com.cargosphere.shipment.integration.container.ContainerAllocationClient;
import com.cargosphere.shipment.integration.container.ContainerAllocationResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentClient;
import com.cargosphere.shipment.integration.document.ShipmentDocumentReadinessResponse;
import com.cargosphere.shipment.integration.document.ShipmentDocumentResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentClient;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentResponse;
import com.cargosphere.shipment.integration.payment.ShipmentPaymentSummaryResponse;
import com.cargosphere.shipment.mapper.EbillSnapshotMapper;
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
import static org.mockito.Mockito.verifyNoInteractions;
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
    private ShipmentActorProvider shipmentActorProvider;

    @Mock
    private CargoDetailRepository cargoDetailRepository;

    @Mock
    private CargoVerificationRepository
            cargoVerificationRepository;

    @Mock
    private AuthUserClient authUserClient;

    @Mock
    private EbillNumberGenerator ebillNumberGenerator;

    @Mock
    private EbillSnapshotJsonService ebillSnapshotJsonService;

    @Mock
    private EbillPdfGenerator ebillPdfGenerator;

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
                shipmentActorProvider,
                cargoDetailRepository,
                cargoVerificationRepository,
                authUserClient,
                new EbillSnapshotMapper(),
                ebillNumberGenerator,
                ebillSnapshotJsonService,
                ebillPdfGenerator,
                containerAllocationClient,
                shipmentDocumentClient,
                shipmentPaymentClient,
                new ProcessingReadinessEvaluator()
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
                .getDocumentReadiness(10L))
                .thenReturn(
                        readyDocumentReadiness()
                );

        when(shipmentPaymentClient
                .getPaymentSummary(10L))
                .thenReturn(
                        readyPaymentSummary()
                );

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
    void shouldReturnCompleteEbillPreview() {
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

        AuthUserResponse client =
                new AuthUserResponse(
                        100L,
                        "Cargo Client",
                        "client@cargosphere.com",
                        "9876543210",
                        "ROLE_CLIENT",
                        "ACTIVE",
                        LocalDateTime.of(
                                2026,
                                7,
                                1,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                1,
                                9,
                                0
                        )
                );

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

        ShipmentEvent event = ShipmentEvent.builder()
                .id(40L)
                .shipment(shipment)
                .eventType(ShipmentEventType.CREATED)
                .eventDescription("Shipment created")
                .eventLocation("Mumbai")
                .eventTime(
                        LocalDateTime.of(
                                2026,
                                8,
                                1,
                                9,
                                0
                        )
                )
                .build();

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(authUserClient.getUserById(100L))
                .thenReturn(client);

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

        when(shipmentDocumentClient
                .getDocumentReadiness(10L))
                .thenReturn(
                        readyDocumentReadiness()
                );

        when(shipmentPaymentClient
                .getPaymentsByShipmentId(10L))
                .thenReturn(List.of(payment));

        when(shipmentPaymentClient
                .getPaymentSummary(10L))
                .thenReturn(
                        readyPaymentSummary()
                );

        when(shipmentEventRepository
                .findByShipment_IdOrderByEventTimeDesc(10L))
                .thenReturn(List.of(event));

        EbillPreviewResponse response =
                service.getEbillPreview(10L);

        assertThat(response.getShipment().shipmentId())
                .isEqualTo(10L);

        assertThat(response.getClient().userId())
                .isEqualTo(100L);

        assertThat(response.getOriginalCargo())
                .hasSize(1);

        assertThat(response.getConfirmedCargo())
                .hasSize(1);

        assertThat(response.getContainerAllocations())
                .hasSize(1);

        assertThat(response.getDocuments())
                .hasSize(1);

        assertThat(response.getPayments())
                .hasSize(1);

        assertThat(response.getShipmentEvents())
                .singleElement()
                .extracting(storedEvent -> storedEvent.eventId())
                .isEqualTo(40L);

        assertThat(response.getReadiness().ebillReady())
                .isTrue();
    }

    @Test
    void shouldGenerateImmutableEbillSuccessfully() {
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
                                        3,
                                        9,
                                        0
                                )
                        )
                        .build();

        AuthUserResponse client =
                new AuthUserResponse(
                        100L,
                        "Cargo Client",
                        "client@cargosphere.com",
                        "9876543210",
                        "ROLE_CLIENT",
                        "ACTIVE",
                        LocalDateTime.of(
                                2026,
                                7,
                                1,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                3,
                                9,
                                0
                        )
                );

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
                                3,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                3,
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
                                3,
                                9,
                                30
                        ),
                        "Verified",
                        LocalDateTime.of(
                                2026,
                                8,
                                3,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                3,
                                9,
                                30
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
                        LocalDate.of(2026, 8, 3),
                        "Payment confirmed",
                        LocalDateTime.of(
                                2026,
                                8,
                                3,
                                9,
                                0
                        ),
                        LocalDateTime.of(
                                2026,
                                8,
                                3,
                                10,
                                0
                        )
                );

        CurrentActor actor =
                new CurrentActor(
                        5L,
                        "ROLE_ADMIN"
                );

        String ebillNumber =
                "EBL-20260803-A1B2C3D4";

        String snapshotJson =
                "{\"schemaVersion\":\"1.0\"}";

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentActorProvider.getCurrentActor())
                .thenReturn(actor);

        when(authUserClient.getUserById(100L))
                .thenReturn(client);

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

        when(shipmentDocumentClient
                .getDocumentReadiness(10L))
                .thenReturn(
                        readyDocumentReadiness()
                );

        when(shipmentPaymentClient
                .getPaymentsByShipmentId(10L))
                .thenReturn(List.of(payment));

        when(shipmentPaymentClient
                .getPaymentSummary(10L))
                .thenReturn(
                        readyPaymentSummary()
                );

        when(shipmentEventRepository
                .findByShipment_IdOrderByEventTimeDesc(10L))
                .thenReturn(List.of());

        when(ebillNumberGenerator.generate(
                any(OffsetDateTime.class)
        ))
                .thenReturn(ebillNumber);

        when(shipmentRepository
                .existsByEbillNumber(ebillNumber))
                .thenReturn(false);

        when(ebillSnapshotJsonService.serialize(
                any(EbillSnapshot.class)
        ))
                .thenReturn(snapshotJson);

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.EBILL_GENERATED
                ))
                .thenReturn(false);

        EbillGenerationResponse response =
                service.generateEbill(10L);

        assertThat(response.getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getShipmentNumber())
                .isEqualTo("SHP-2026-00010");

        assertThat(response.getEbillNumber())
                .isEqualTo(ebillNumber);

        assertThat(response.getEbillVersion())
                .isEqualTo(1);

        assertThat(response.getGeneratedBy())
                .isEqualTo(5L);

        assertThat(response.getGeneratedAt())
                .isNotNull();

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.EBILL_GENERATED
                );

        assertThat(shipment.getEbillNumber())
                .isEqualTo(ebillNumber);

        assertThat(shipment.getEbillVersion())
                .isEqualTo(1);

        assertThat(shipment.getEbillGeneratedBy())
                .isEqualTo(5L);

        assertThat(shipment.getEbillGeneratedAt())
                .isNotNull();

        assertThat(shipment.getEbillSnapshot())
                .isEqualTo(snapshotJson);

        assertThat(shipment.getProcessingCompletedAt())
                .isEqualTo(
                        shipment.getEbillGeneratedAt()
                );

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.EBILL_GENERATED
                );

        ArgumentCaptor<EbillSnapshot> snapshotCaptor =
                ArgumentCaptor.forClass(
                        EbillSnapshot.class
                );

        verify(ebillSnapshotJsonService)
                .serialize(snapshotCaptor.capture());

        EbillSnapshot capturedSnapshot =
                snapshotCaptor.getValue();

        assertThat(capturedSnapshot.ebillNumber())
                .isEqualTo(ebillNumber);

        assertThat(capturedSnapshot.ebillVersion())
                .isEqualTo(1);

        assertThat(capturedSnapshot.generatedBy())
                .isEqualTo(5L);

        assertThat(capturedSnapshot.generatedAt())
                .isEqualTo(
                        shipment.getEbillGeneratedAt()
                );

        assertThat(capturedSnapshot.readiness().ebillReady())
                .isTrue();

        verify(shipmentRepository)
                .save(shipment);

        ArgumentCaptor<ShipmentEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        ShipmentEvent.class
                );

        verify(shipmentEventRepository)
                .save(eventCaptor.capture());

        ShipmentEvent generatedEvent =
                eventCaptor.getValue();

        assertThat(generatedEvent.getEventType())
                .isEqualTo(
                        ShipmentEventType.EBILL_GENERATED
                );

        assertThat(generatedEvent.getEventDescription())
                .contains(ebillNumber);

        verify(shipmentAuditPublisher)
                .publishEbillGenerated(shipment);
    }

    @Test
    void shouldReturnExistingEbillWithoutRegenerating() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        10,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        Shipment shipment = createShipment(
                ProcessingStage.EBILL_GENERATED
        );

        shipment.setEbillNumber(
                "EBL-20260803-A1B2C3D4"
        );

        shipment.setEbillVersion(1);
        shipment.setEbillGeneratedAt(generatedAt);
        shipment.setEbillGeneratedBy(5L);
        shipment.setEbillSnapshot(
                "{\"schemaVersion\":\"1.0\"}"
        );
        shipment.setProcessingCompletedAt(generatedAt);

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        EbillGenerationResponse response =
                service.generateEbill(10L);

        assertThat(response.getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getShipmentNumber())
                .isEqualTo("SHP-2026-00010");

        assertThat(response.getEbillNumber())
                .isEqualTo(
                        "EBL-20260803-A1B2C3D4"
                );

        assertThat(response.getEbillVersion())
                .isEqualTo(1);

        assertThat(response.getGeneratedAt())
                .isEqualTo(generatedAt);

        assertThat(response.getGeneratedBy())
                .isEqualTo(5L);

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.EBILL_GENERATED
                );

        verifyNoInteractions(
                shipmentActorProvider,
                authUserClient,
                containerAllocationClient,
                cargoDetailRepository,
                cargoVerificationRepository,
                shipmentDocumentClient,
                shipmentPaymentClient,
                ebillNumberGenerator,
                ebillSnapshotJsonService,
                shipmentEventRepository,
                shipmentAuditPublisher
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));
    }

    @Test
    void shouldRejectEbillGenerationBeforeReadyStage() {
        Shipment shipment = createShipment(
                ProcessingStage.CONTAINER_ALLOCATION
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
                service.generateEbill(10L)
        )
                .isInstanceOf(
                        InvalidProcessingStageException.class
                )
                .hasMessage(
                        "Shipment 10 must be in processing stage "
                                + "READY_FOR_EBILL, but current stage is "
                                + "CONTAINER_ALLOCATION"
                );

        verifyNoInteractions(
                shipmentActorProvider,
                authUserClient,
                containerAllocationClient,
                cargoDetailRepository,
                cargoVerificationRepository,
                shipmentDocumentClient,
                shipmentPaymentClient,
                ebillNumberGenerator,
                ebillSnapshotJsonService,
                shipmentEventRepository,
                shipmentAuditPublisher
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));
    }

    @Test
    void shouldRejectEbillGenerationWithoutAuthenticatedAdmin() {
        Shipment shipment = createShipment(
                ProcessingStage.READY_FOR_EBILL
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentActorProvider.getCurrentActor())
                .thenReturn(CurrentActor.anonymous());

        assertThatThrownBy(() ->
                service.generateEbill(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "An authenticated administrator is required "
                                + "to generate an eBill"
                );

        verifyNoInteractions(
                authUserClient,
                containerAllocationClient,
                cargoDetailRepository,
                cargoVerificationRepository,
                shipmentDocumentClient,
                shipmentPaymentClient,
                ebillNumberGenerator,
                ebillSnapshotJsonService,
                shipmentEventRepository,
                shipmentAuditPublisher
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));
    }

    @Test
    void shouldRejectEbillGenerationWhenReadinessIsBlocked() {
        Shipment shipment = createShipment(
                ProcessingStage.READY_FOR_EBILL
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentActorProvider.getCurrentActor())
                .thenReturn(
                        new CurrentActor(
                                5L,
                                "ROLE_ADMIN"
                        )
                );

        when(containerAllocationClient
                .getAllocationsByShipmentId(10L))
                .thenReturn(List.of());

        when(cargoDetailRepository
                .findByShipment_Id(10L))
                .thenReturn(List.of());

        when(cargoVerificationRepository
                .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                        10L
                ))
                .thenReturn(List.of());

        when(shipmentDocumentClient
                .getDocumentsByShipmentId(10L))
                .thenReturn(List.of());

        when(shipmentDocumentClient
                .getDocumentReadiness(10L))
                .thenReturn(
                        blockedDocumentReadiness()
                );

        when(shipmentPaymentClient
                .getPaymentsByShipmentId(10L))
                .thenReturn(List.of());

        when(shipmentEventRepository
                .findByShipment_IdOrderByEventTimeDesc(10L))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                service.generateEbill(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "Shipment 10 is not ready for eBill generation: "
                                + "No valid container allocation exists; "
                                + "All shipment cargo items must be confirmed; "
                                + "Required documents are unresolved: "
                                + "COMMERCIAL_INVOICE, PACKING_LIST; "
                                + "A shipment payment summary is required"
                );

        verifyNoInteractions(
                ebillNumberGenerator,
                ebillSnapshotJsonService,
                shipmentAuditPublisher
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }

    @Test
    void shouldGenerateEbillPdfFromStoredImmutableSnapshot() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        10,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        Shipment shipment = createShipment(
                ProcessingStage.EBILL_GENERATED
        );

        shipment.setEbillNumber(
                "EBL-20260803-A1B2C3D4"
        );
        shipment.setEbillVersion(1);
        shipment.setEbillGeneratedAt(generatedAt);
        shipment.setEbillGeneratedBy(5L);
        shipment.setEbillSnapshot(
                "{\"schemaVersion\":\"1.0\"}"
        );
        shipment.setProcessingCompletedAt(generatedAt);

        EbillSnapshot snapshot =
                new EbillSnapshot(
                        "1.0",
                        "EBL-20260803-A1B2C3D4",
                        1,
                        generatedAt,
                        5L,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        null
                );

        byte[] pdfContent =
                new byte[] {
                        37,
                        80,
                        68,
                        70,
                        45
                };

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(ebillSnapshotJsonService.deserialize(
                "{\"schemaVersion\":\"1.0\"}"
        )).thenReturn(snapshot);

        when(ebillPdfGenerator.generate(snapshot))
                .thenReturn(pdfContent);

        EbillPdfDocument document =
                service.getEbillPdf(10L);

        assertThat(document.fileName())
                .isEqualTo(
                        "EBL-20260803-A1B2C3D4.pdf"
                );

        assertThat(document.content())
                .isEqualTo(pdfContent);

        assertThat(document.content())
                .isNotSameAs(pdfContent);

        verify(ebillSnapshotJsonService)
                .deserialize(
                        "{\"schemaVersion\":\"1.0\"}"
                );

        verify(ebillPdfGenerator)
                .generate(snapshot);

        verifyNoInteractions(
                shipmentActorProvider,
                authUserClient,
                containerAllocationClient,
                cargoDetailRepository,
                cargoVerificationRepository,
                shipmentDocumentClient,
                shipmentPaymentClient,
                ebillNumberGenerator,
                shipmentEventRepository,
                shipmentAuditPublisher
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));
    }

    @Test
    void shouldRejectEbillPdfWhenEbillHasNotBeenGenerated() {
        Shipment shipment = createShipment(
                ProcessingStage.READY_FOR_EBILL
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
                service.getEbillPdf(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "Shipment 10 does not have a generated eBill"
                );

        verifyNoInteractions(
                ebillSnapshotJsonService,
                ebillPdfGenerator
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));
    }

    @Test
    void shouldRejectEbillPdfWhenStoredSnapshotMetadataDoesNotMatch() {
        OffsetDateTime generatedAt =
                OffsetDateTime.of(
                        2026,
                        8,
                        3,
                        10,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        Shipment shipment = createShipment(
                ProcessingStage.EBILL_GENERATED
        );

        shipment.setEbillNumber(
                "EBL-20260803-A1B2C3D4"
        );
        shipment.setEbillVersion(1);
        shipment.setEbillGeneratedAt(generatedAt);
        shipment.setEbillGeneratedBy(5L);
        shipment.setEbillSnapshot(
                "{\"schemaVersion\":\"1.0\"}"
        );

        EbillSnapshot mismatchedSnapshot =
                new EbillSnapshot(
                        "1.0",
                        "EBL-20260803-DIFFERENT",
                        1,
                        generatedAt,
                        5L,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        null
                );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(ebillSnapshotJsonService.deserialize(
                "{\"schemaVersion\":\"1.0\"}"
        )).thenReturn(mismatchedSnapshot);

        assertThatThrownBy(() ->
                service.getEbillPdf(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "Stored eBill snapshot does not match "
                                + "shipment metadata for shipment 10"
                );

        verify(ebillSnapshotJsonService)
                .deserialize(
                        "{\"schemaVersion\":\"1.0\"}"
                );

        verifyNoInteractions(
                ebillPdfGenerator
        );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));
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
                .getDocumentReadiness(10L))
                .thenReturn(
                        blockedDocumentReadiness()
                );

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
                        "Required documents are unresolved: "
                                + "COMMERCIAL_INVOICE, PACKING_LIST",
                        "A shipment payment summary is required"
                );
    }
    @Test
    void shouldContinueFromContainerAllocationToCargoVerification() {
        Shipment shipment = createShipment(
                ProcessingStage.CONTAINER_ALLOCATION
        );

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

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(containerAllocationClient
                .getAllocationsByShipmentId(10L))
                .thenReturn(List.of(allocation));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.CONTAINER_ALLOCATED
                ))
                .thenReturn(false);

        ProcessingContinueResponse response =
                service.continueProcessing(10L);

        assertThat(response.getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getPreviousStage())
                .isEqualTo(
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CARGO_VERIFICATION
                );

        assertThat(response.getAdvancedAt())
                .isNotNull();

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CARGO_VERIFICATION
                );

        verify(containerAllocationClient)
                .getAllocationsByShipmentId(10L);

        verify(shipmentRepository)
                .save(shipment);

        ArgumentCaptor<ShipmentEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        ShipmentEvent.class
                );

        verify(shipmentEventRepository)
                .save(eventCaptor.capture());

        ShipmentEvent savedEvent =
                eventCaptor.getValue();

        assertThat(savedEvent.getEventType())
                .isEqualTo(
                        ShipmentEventType.CONTAINER_ALLOCATED
                );

        assertThat(savedEvent.getShipment())
                .isSameAs(shipment);
    }
    @Test
    void shouldBlockContinueWhenContainerAllocationIsInvalid() {
        Shipment shipment = createShipment(
                ProcessingStage.CONTAINER_ALLOCATION
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(containerAllocationClient
                .getAllocationsByShipmentId(10L))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                service.continueProcessing(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "A valid container allocation is required "
                                + "before continuing shipment 10"
                );

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CONTAINER_ALLOCATION
                );

        verify(containerAllocationClient)
                .getAllocationsByShipmentId(10L);

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }
    @Test
    void shouldContinueFromDocumentVerificationToPaymentConfirmation() {
        Shipment shipment = createShipment(
                ProcessingStage.DOCUMENT_VERIFICATION
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

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentDocumentClient
                .getDocumentReadiness(10L))
                .thenReturn(
                        readyDocumentReadiness()
                );

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.DOCUMENTS_VERIFIED
                ))
                .thenReturn(false);

        ProcessingContinueResponse response =
                service.continueProcessing(10L);

        assertThat(response.getPreviousStage())
                .isEqualTo(
                        ProcessingStage.DOCUMENT_VERIFICATION
                );

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.PAYMENT_CONFIRMATION
                );

        assertThat(response.getAdvancedAt())
                .isNotNull();

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.PAYMENT_CONFIRMATION
                );

        verify(shipmentDocumentClient)
                .getDocumentReadiness(10L);

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
                        ShipmentEventType.DOCUMENTS_VERIFIED
                );

        assertThat(eventCaptor.getValue().getShipment())
                .isSameAs(shipment);
    }
    @Test
    void shouldContinueFromPaymentConfirmationToReadyForEbill() {
        Shipment shipment = createShipment(
                ProcessingStage.PAYMENT_CONFIRMATION
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

        when(shipmentPaymentClient
                .getPaymentSummary(10L))
                .thenReturn(
                        readyPaymentSummary()
                );

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.PAYMENT_CONFIRMED
                ))
                .thenReturn(false);

        ProcessingContinueResponse response =
                service.continueProcessing(10L);

        assertThat(response.getPreviousStage())
                .isEqualTo(
                        ProcessingStage.PAYMENT_CONFIRMATION
                );

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.READY_FOR_EBILL
                );

        assertThat(response.getAdvancedAt())
                .isNotNull();

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.READY_FOR_EBILL
                );

        verify(shipmentPaymentClient)
                .getPaymentSummary(10L);

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
                        ShipmentEventType.PAYMENT_CONFIRMED
                );

        assertThat(eventCaptor.getValue().getShipment())
                .isSameAs(shipment);
    }
    @Test
    void shouldBlockContinueWhenRequiredDocumentsAreNotVerified() {
        Shipment shipment = createShipment(
                ProcessingStage.DOCUMENT_VERIFICATION
        );

        ShipmentDocumentResponse document =
                new ShipmentDocumentResponse(
                        1L,
                        10L,
                        "COMMERCIAL_INVOICE",
                        true,
                        "PENDING",
                        null,
                        null,
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

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentDocumentClient
                .getDocumentReadiness(10L))
                .thenReturn(
                        blockedDocumentReadiness()
                );

        assertThatThrownBy(() ->
                service.continueProcessing(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "All required shipment documents must be "
                                + "VERIFIED or NOT_APPLICABLE before "
                                + "continuing shipment 10. Blocking documents: "
                                + "COMMERCIAL_INVOICE, PACKING_LIST"
                );

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.DOCUMENT_VERIFICATION
                );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }
    @Test
    void shouldBlockContinueWhenValidPaidPaymentDoesNotExist() {
        Shipment shipment = createShipment(
                ProcessingStage.PAYMENT_CONFIRMATION
        );

        ShipmentPaymentResponse payment =
                new ShipmentPaymentResponse(
                        1L,
                        10L,
                        100L,
                        new BigDecimal("12500.00"),
                        "INR",
                        "UPI",
                        "PENDING",
                        "FULL",
                        null,
                        LocalDate.of(2026, 8, 15),
                        null,
                        "Payment pending",
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

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(shipmentPaymentClient
                .getPaymentSummary(10L))
                .thenReturn(
                        draftPaymentSummary()
                );

        assertThatThrownBy(() ->
                service.continueProcessing(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "Shipment payment summary is not confirmed "
                                + "before continuing shipment 10"
                );

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.PAYMENT_CONFIRMATION
                );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));
    }
    @Test
    void shouldRequireCargoVerificationEndpointAtCargoVerificationStage() {
        Shipment shipment = createShipment(
                ProcessingStage.CARGO_VERIFICATION
        );

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
                service.continueProcessing(10L)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessage(
                        "Cargo verification must be confirmed "
                                + "through the cargo verification endpoint"
                );

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CARGO_VERIFICATION
                );

        verify(shipmentRepository, never())
                .save(any(Shipment.class));

        verify(shipmentEventRepository, never())
                .save(any(ShipmentEvent.class));

        verify(containerAllocationClient, never())
                .getAllocationsByShipmentId(any());

        verify(shipmentDocumentClient, never())
                .getDocumentReadiness(any());

        verify(shipmentPaymentClient, never())
                .getPaymentSummary(any());
    }
    @Test
    void shouldPublishAuditAfterSuccessfulProcessingTransition() {
        Shipment shipment = createShipment(
                ProcessingStage.CONTAINER_ALLOCATION
        );

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

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(containerAllocationClient
                .getAllocationsByShipmentId(10L))
                .thenReturn(List.of(allocation));

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.CONTAINER_ALLOCATED
                ))
                .thenReturn(true);

        service.continueProcessing(10L);

        verify(shipmentAuditPublisher)
                .publishProcessingAdvanced(
                        shipment,
                        ProcessingStage.CONTAINER_ALLOCATION
                );
    }
    private ShipmentPaymentSummaryResponse
    readyPaymentSummary() {
        return new ShipmentPaymentSummaryResponse(
                1L,
                10L,
                new BigDecimal("12500.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                new BigDecimal("12500.00"),
                new BigDecimal("12500.00"),
                BigDecimal.ZERO,
                "INR",
                "UPI",
                "CONFIRMED",
                5L,
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        10,
                        0
                ),
                "Payment confirmed",
                true,
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        10,
                        0
                )
        );
    }

    private ShipmentPaymentSummaryResponse
    draftPaymentSummary() {
        return new ShipmentPaymentSummaryResponse(
                1L,
                10L,
                new BigDecimal("12500.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                new BigDecimal("12500.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("7500.00"),
                "INR",
                "UPI",
                "DRAFT",
                null,
                null,
                "Payment pending",
                false,
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        9,
                        0
                )
        );
    }

    private ShipmentDocumentReadinessResponse
    readyDocumentReadiness() {
        return new ShipmentDocumentReadinessResponse(
                10L,
                5,
                4,
                3,
                1,
                0,
                true,
                List.of()
        );
    }

    private ShipmentDocumentReadinessResponse
    blockedDocumentReadiness() {
        return new ShipmentDocumentReadinessResponse(
                10L,
                5,
                4,
                2,
                0,
                2,
                false,
                List.of(
                        "COMMERCIAL_INVOICE",
                        "PACKING_LIST"
                )
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