package com.cargosphere.shipment.service.impl;

import com.cargosphere.shipment.audit.CurrentActor;
import com.cargosphere.shipment.audit.ShipmentActorProvider;
import com.cargosphere.shipment.audit.ShipmentAuditPublisher;
import com.cargosphere.shipment.dto.admin.CargoVerificationAction;
import com.cargosphere.shipment.dto.admin.CargoVerificationItemRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.exception.InvalidProcessingStageException;
import com.cargosphere.shipment.exception.InvalidShipmentOperationException;
import com.cargosphere.shipment.mapper.CargoVerificationMapper;
import com.cargosphere.shipment.mapper.ShipmentEventMapper;
import com.cargosphere.shipment.repository.CargoDetailRepository;
import com.cargosphere.shipment.repository.CargoVerificationRepository;
import com.cargosphere.shipment.repository.ShipmentEventRepository;
import com.cargosphere.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CargoVerificationServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CargoDetailRepository cargoDetailRepository;

    @Mock
    private CargoVerificationRepository
            cargoVerificationRepository;

    @Mock
    private ShipmentEventRepository shipmentEventRepository;

    @Mock
    private ShipmentActorProvider shipmentActorProvider;

    @Mock
    private ShipmentAuditPublisher shipmentAuditPublisher;

    private CargoVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CargoVerificationServiceImpl(
                shipmentRepository,
                cargoDetailRepository,
                cargoVerificationRepository,
                shipmentEventRepository,
                new CargoVerificationMapper(),
                new ShipmentEventMapper(),
                shipmentActorProvider,
                shipmentAuditPublisher
        );
    }

    @Test
    void shouldSaveCargoVerificationAsDraft() {
        Shipment shipment = createShipment(
                ProcessingStage.CARGO_VERIFICATION
        );

        CargoDetail cargoDetail =
                createCargoDetail(shipment);

        CargoVerification storedVerification =
                CargoVerification.builder()
                        .id(21L)
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.DRAFT
                        )
                        .confirmedCargoName("Electronics Box")
                        .build();

        CargoVerificationRequest request =
                CargoVerificationRequest.builder()
                        .action(
                                CargoVerificationAction.SAVE_DRAFT
                        )
                        .items(List.of(
                                CargoVerificationItemRequest.builder()
                                        .cargoDetailId(11L)
                                        .confirmedCargoName(
                                                "Electronics Box"
                                        )
                                        .build()
                        ))
                        .build();

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(cargoDetailRepository.findByShipment_Id(10L))
                .thenReturn(List.of(cargoDetail));

        when(cargoVerificationRepository
                .findByCargoDetail_IdIn(List.of(11L)))
                .thenReturn(List.of());

        when(cargoVerificationRepository.saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(cargoVerificationRepository
                .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                        10L
                ))
                .thenReturn(List.of(storedVerification));

        CargoVerificationResponse response =
                service.saveOrConfirm(10L, request);

        assertThat(response.getShipmentId())
                .isEqualTo(10L);

        assertThat(response.getAction())
                .isEqualTo(
                        CargoVerificationAction.SAVE_DRAFT
                );

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CARGO_VERIFICATION
                );

        assertThat(response.getItems())
                .hasSize(1);

        assertThat(response.getItems().getFirst()
                .getVerificationStatus())
                .isEqualTo(
                        CargoVerificationStatus.DRAFT
                );

        ArgumentCaptor<List<CargoVerification>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(cargoVerificationRepository)
                .saveAll(captor.capture());

        CargoVerification savedVerification =
                captor.getValue().getFirst();

        assertThat(savedVerification.getVerificationStatus())
                .isEqualTo(
                        CargoVerificationStatus.DRAFT
                );

        assertThat(savedVerification.getVerifiedBy())
                .isNull();

        assertThat(savedVerification.getVerifiedAt())
                .isNull();

        verify(shipmentRepository, never())
                .save(shipment);

        verify(shipmentEventRepository, never())
                .save(org.mockito.ArgumentMatchers
                        .any(ShipmentEvent.class));

        verify(shipmentAuditPublisher, never())
                .publishCargoVerified(shipment);
    }

    @Test
    void shouldConfirmCargoAndAdvanceToDocumentVerification() {
        Shipment shipment = createShipment(
                ProcessingStage.CARGO_VERIFICATION
        );

        CargoDetail cargoDetail =
                createCargoDetail(shipment);

        CargoVerification verification =
                CargoVerification.builder()
                        .id(21L)
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.DRAFT
                        )
                        .build();

        CargoVerificationRequest request =
                createCompleteConfirmationRequest();

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(cargoDetailRepository.findByShipment_Id(10L))
                .thenReturn(List.of(cargoDetail));

        when(cargoVerificationRepository
                .findByCargoDetail_IdIn(List.of(11L)))
                .thenReturn(List.of(verification));

        when(cargoVerificationRepository.saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(cargoVerificationRepository
                .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                        10L
                ))
                .thenReturn(List.of(verification));

        when(shipmentActorProvider.getCurrentActor())
                .thenReturn(
                        new CurrentActor(
                                5L,
                                "ROLE_ADMIN"
                        )
                );

        when(shipmentRepository.save(shipment))
                .thenReturn(shipment);

        when(shipmentEventRepository
                .existsByShipment_IdAndEventType(
                        10L,
                        ShipmentEventType.CARGO_VERIFIED
                ))
                .thenReturn(false);

        CargoVerificationResponse response =
                service.saveOrConfirm(10L, request);

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.DOCUMENT_VERIFICATION
                );

        assertThat(verification.getVerificationStatus())
                .isEqualTo(
                        CargoVerificationStatus.CONFIRMED
                );

        assertThat(verification.getVerifiedBy())
                .isEqualTo(5L);

        assertThat(verification.getVerifiedAt())
                .isNotNull();

        assertThat(response.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.DOCUMENT_VERIFICATION
                );

        assertThat(response.getAction())
                .isEqualTo(
                        CargoVerificationAction
                                .CONFIRM_AND_CONTINUE
                );

        verify(cargoVerificationRepository, times(2))
                .saveAll(anyList());

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
                        ShipmentEventType.CARGO_VERIFIED
                );

        assertThat(eventCaptor.getValue().getShipment())
                .isSameAs(shipment);

        verify(shipmentAuditPublisher)
                .publishCargoVerified(shipment);
    }

    @Test
    void shouldRejectVerificationAtIncorrectProcessingStage() {
        Shipment shipment = createShipment(
                ProcessingStage.CONTAINER_ALLOCATION
        );

        CargoVerificationRequest request =
                createCompleteConfirmationRequest();

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
                service.saveOrConfirm(10L, request)
        )
                .isInstanceOf(
                        InvalidProcessingStageException.class
                )
                .hasMessageContaining(
                        "CARGO_VERIFICATION"
                )
                .hasMessageContaining(
                        "CONTAINER_ALLOCATION"
                );

        verify(cargoDetailRepository, never())
                .findByShipment_Id(10L);

        verify(shipmentRepository, never())
                .save(shipment);

        verify(shipmentAuditPublisher, never())
                .publishCargoVerified(shipment);
    }

    @Test
    void shouldRejectConfirmationWhenRequiredValueIsMissing() {
        Shipment shipment = createShipment(
                ProcessingStage.CARGO_VERIFICATION
        );

        CargoDetail cargoDetail =
                createCargoDetail(shipment);

        CargoVerification verification =
                CargoVerification.builder()
                        .id(21L)
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.DRAFT
                        )
                        .build();

        CargoVerificationRequest request =
                CargoVerificationRequest.builder()
                        .action(
                                CargoVerificationAction
                                        .CONFIRM_AND_CONTINUE
                        )
                        .items(List.of(
                                CargoVerificationItemRequest.builder()
                                        .cargoDetailId(11L)
                                        .confirmedCargoName(null)
                                        .confirmedCargoType(
                                                CargoType.ELECTRONICS
                                        )
                                        .confirmedWeightKg(
                                                new BigDecimal(
                                                        "25.500"
                                                )
                                        )
                                        .confirmedVolumeCbm(
                                                new BigDecimal(
                                                        "1.200"
                                                )
                                        )
                                        .confirmedQuantity(2)
                                        .confirmedFragile(true)
                                        .confirmedHazardous(false)
                                        .build()
                        ))
                        .build();

        when(shipmentRepository.findById(10L))
                .thenReturn(Optional.of(shipment));

        when(cargoDetailRepository.findByShipment_Id(10L))
                .thenReturn(List.of(cargoDetail));

        when(cargoVerificationRepository
                .findByCargoDetail_IdIn(List.of(11L)))
                .thenReturn(List.of(verification));

        when(cargoVerificationRepository.saveAll(anyList()))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(cargoVerificationRepository
                .findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
                        10L
                ))
                .thenReturn(List.of(verification));

        when(shipmentActorProvider.getCurrentActor())
                .thenReturn(
                        new CurrentActor(
                                5L,
                                "ROLE_ADMIN"
                        )
                );

        assertThatThrownBy(() ->
                service.saveOrConfirm(10L, request)
        )
                .isInstanceOf(
                        InvalidShipmentOperationException.class
                )
                .hasMessageContaining(
                        "Confirmed cargo name is required"
                )
                .hasMessageContaining(
                        "11"
                );

        assertThat(shipment.getProcessingStage())
                .isEqualTo(
                        ProcessingStage.CARGO_VERIFICATION
                );

        verify(shipmentRepository, never())
                .save(shipment);

        verify(shipmentEventRepository, never())
                .save(org.mockito.ArgumentMatchers
                        .any(ShipmentEvent.class));

        verify(shipmentAuditPublisher, never())
                .publishCargoVerified(shipment);
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

    private CargoDetail createCargoDetail(
            Shipment shipment
    ) {
        return CargoDetail.builder()
                .id(11L)
                .shipment(shipment)
                .cargoName("Client Submitted Electronics")
                .cargoType(CargoType.ELECTRONICS)
                .weightKg(new BigDecimal("25.000"))
                .volumeCbm(new BigDecimal("1.000"))
                .quantity(2)
                .fragile(true)
                .hazardous(false)
                .build();
    }

    private CargoVerificationRequest
    createCompleteConfirmationRequest() {
        return CargoVerificationRequest.builder()
                .action(
                        CargoVerificationAction
                                .CONFIRM_AND_CONTINUE
                )
                .items(List.of(
                        CargoVerificationItemRequest.builder()
                                .cargoDetailId(11L)
                                .confirmedCargoName(
                                        "Electronics Box"
                                )
                                .confirmedCargoDescription(
                                        "Verified laptop accessories"
                                )
                                .confirmedCargoType(
                                        CargoType.ELECTRONICS
                                )
                                .confirmedWeightKg(
                                        new BigDecimal("25.500")
                                )
                                .confirmedVolumeCbm(
                                        new BigDecimal("1.200")
                                )
                                .confirmedQuantity(2)
                                .confirmedFragile(true)
                                .confirmedHazardous(false)
                                .verificationRemarks(
                                        "Physical verification complete"
                                )
                                .build()
                ))
                .build();
    }
}