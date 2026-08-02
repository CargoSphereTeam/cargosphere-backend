package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.config.SecurityConfig;
import com.cargosphere.shipment.dto.admin.CargoVerificationAction;
import com.cargosphere.shipment.dto.admin.CargoVerificationItemRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;
import com.cargosphere.shipment.dto.admin.ProcessingContinueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueItemResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.dto.ebill.EbillPreviewResponse;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillClientSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillReadinessSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillShipmentSnapshot;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.exception.InvalidProcessingStageException;
import com.cargosphere.shipment.exception.InvalidShipmentOperationException;
import com.cargosphere.shipment.service.AdminShipmentProcessingService;
import com.cargosphere.shipment.service.CargoVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminShipmentProcessingController.class)
@Import(SecurityConfig.class)
class AdminShipmentProcessingControllerTest {

    private static final String CARGO_VERIFICATION_ENDPOINT =
            "/api/admin/shipments/10/cargo-verification";

    private static final String START_PROCESSING_ENDPOINT =
            "/api/admin/shipments/10/processing/start";

    private static final String PROCESSING_QUEUE_ENDPOINT =
            "/api/admin/shipments/processing/queue";

    private static final String PROCESSING_READINESS_ENDPOINT =
            "/api/admin/shipments/10/processing/readiness";

    private static final String EBILL_PREVIEW_ENDPOINT =
            "/api/admin/shipments/10/ebill-preview";
    private static final String PROCESSING_CONTINUE_ENDPOINT =
            "/api/admin/shipments/10/processing/continue";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CargoVerificationService cargoVerificationService;

    @MockBean
    private AdminShipmentProcessingService
            adminShipmentProcessingService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldAllowAdminToStartProcessing() throws Exception {
        ProcessingStartResponse response =
                ProcessingStartResponse.builder()
                        .shipmentId(10L)
                        .shipmentNumber("SHP-2026-00010")
                        .processingStage(
                                ProcessingStage.CONTAINER_ALLOCATION
                        )
                        .processingStartedAt(
                                OffsetDateTime.of(
                                        2026,
                                        8,
                                        2,
                                        8,
                                        0,
                                        0,
                                        0,
                                        ZoneOffset.UTC
                                )
                        )
                        .build();

        when(adminShipmentProcessingService.startProcessing(10L))
                .thenReturn(response);

        mockMvc.perform(
                        post(START_PROCESSING_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(10))
                .andExpect(
                        jsonPath("$.shipmentNumber")
                                .value("SHP-2026-00010")
                )
                .andExpect(
                        jsonPath("$.processingStage")
                                .value("CONTAINER_ALLOCATION")
                )
                .andExpect(
                        jsonPath("$.processingStartedAt")
                                .exists()
                );

        verify(adminShipmentProcessingService)
                .startProcessing(10L);
    }

    @Test
    void shouldForbidClientFromStartingProcessing()
            throws Exception {

        mockMvc.perform(
                        post(START_PROCESSING_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENT"
                                        )
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShipmentProcessingService);
    }

    @Test
    void shouldRejectAnonymousProcessingStart()
            throws Exception {

        mockMvc.perform(
                        post(START_PROCESSING_ENDPOINT)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminShipmentProcessingService);
    }

    @Test
    void shouldReturnConflictWhenProcessingAlreadyStarted()
            throws Exception {

        when(adminShipmentProcessingService.startProcessing(10L))
                .thenThrow(
                        new InvalidProcessingStageException(
                                10L,
                                ProcessingStage.PENDING_ADMIN_REVIEW,
                                ProcessingStage.CONTAINER_ALLOCATION
                        )
                );

        mockMvc.perform(
                        post(START_PROCESSING_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PROCESSING_STAGE")
                )
                .andExpect(
                        jsonPath("$.currentStage")
                                .value("CONTAINER_ALLOCATION")
                );
    }

    @Test
    void shouldAllowAdminToSaveCargoVerification()
            throws Exception {

        CargoVerificationRequest request =
                createValidCargoVerificationRequest();

        CargoVerificationResponse response =
                CargoVerificationResponse.builder()
                        .shipmentId(10L)
                        .action(
                                CargoVerificationAction.SAVE_DRAFT
                        )
                        .processingStage(
                                ProcessingStage.CARGO_VERIFICATION
                        )
                        .items(List.of())
                        .build();

        when(cargoVerificationService.saveOrConfirm(
                eq(10L),
                any(CargoVerificationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(CARGO_VERIFICATION_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(10))
                .andExpect(
                        jsonPath("$.action")
                                .value("SAVE_DRAFT")
                )
                .andExpect(
                        jsonPath("$.processingStage")
                                .value("CARGO_VERIFICATION")
                );

        verify(cargoVerificationService)
                .saveOrConfirm(
                        eq(10L),
                        any(CargoVerificationRequest.class)
                );
    }

    @Test
    void shouldForbidClientFromCargoVerification()
            throws Exception {

        CargoVerificationRequest request =
                createValidCargoVerificationRequest();

        mockMvc.perform(
                        put(CARGO_VERIFICATION_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENT"
                                        )
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(cargoVerificationService);
    }

    @Test
    void shouldRejectAnonymousCargoVerification()
            throws Exception {

        CargoVerificationRequest request =
                createValidCargoVerificationRequest();

        mockMvc.perform(
                        put(CARGO_VERIFICATION_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cargoVerificationService);
    }

    @Test
    void shouldRejectInvalidCargoVerificationRequest()
            throws Exception {

        String invalidRequest = """
                {
                  "items": []
                }
                """;

        mockMvc.perform(
                        put(CARGO_VERIFICATION_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath("$.validationErrors.action")
                                .value(
                                        "Cargo verification action is required"
                                )
                )
                .andExpect(
                        jsonPath("$.validationErrors.items")
                                .value(
                                        "At least one cargo verification item is required"
                                )
                );

        verifyNoInteractions(cargoVerificationService);
    }

    @Test
    void shouldReturnConflictForIncorrectCargoVerificationStage()
            throws Exception {

        CargoVerificationRequest request =
                createValidCargoVerificationRequest();

        when(cargoVerificationService.saveOrConfirm(
                eq(10L),
                any(CargoVerificationRequest.class)
        )).thenThrow(
                new InvalidProcessingStageException(
                        10L,
                        ProcessingStage.CARGO_VERIFICATION,
                        ProcessingStage.CONTAINER_ALLOCATION
                )
        );

        mockMvc.perform(
                        put(CARGO_VERIFICATION_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PROCESSING_STAGE")
                )
                .andExpect(
                        jsonPath("$.currentStage")
                                .value("CONTAINER_ALLOCATION")
                );
    }

    @Test
    void shouldAllowAdminToGetProcessingQueue()
            throws Exception {

        ProcessingQueueItemResponse item =
                ProcessingQueueItemResponse.builder()
                        .shipmentId(10L)
                        .shipmentNumber("SHP-2026-00010")
                        .clientUserId(100L)
                        .originLocation("Mumbai")
                        .destinationLocation("Pune")
                        .processingStage(
                                ProcessingStage.PENDING_ADMIN_REVIEW
                        )
                        .createdAt(
                                OffsetDateTime.of(
                                        2026,
                                        8,
                                        1,
                                        8,
                                        0,
                                        0,
                                        0,
                                        ZoneOffset.UTC
                                )
                        )
                        .build();

        ProcessingQueueResponse response =
                ProcessingQueueResponse.builder()
                        .items(List.of(item))
                        .page(0)
                        .size(20)
                        .totalElements(1)
                        .totalPages(1)
                        .first(true)
                        .last(true)
                        .empty(false)
                        .build();

        when(adminShipmentProcessingService
                .getProcessingQueue(
                        null,
                        0,
                        20
                ))
                .thenReturn(response);

        mockMvc.perform(
                        get(PROCESSING_QUEUE_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(
                        jsonPath("$.items[0].shipmentId")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.items[0].shipmentNumber")
                                .value("SHP-2026-00010")
                )
                .andExpect(
                        jsonPath("$.items[0].processingStage")
                                .value("PENDING_ADMIN_REVIEW")
                )
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.empty").value(false));

        verify(adminShipmentProcessingService)
                .getProcessingQueue(
                        null,
                        0,
                        20
                );
    }

    @Test
    void shouldPassStageAndPaginationToQueueService()
            throws Exception {

        ProcessingQueueResponse response =
                ProcessingQueueResponse.builder()
                        .items(List.of())
                        .page(2)
                        .size(10)
                        .totalElements(0)
                        .totalPages(0)
                        .first(false)
                        .last(true)
                        .empty(true)
                        .build();

        when(adminShipmentProcessingService
                .getProcessingQueue(
                        ProcessingStage.CARGO_VERIFICATION,
                        2,
                        10
                ))
                .thenReturn(response);

        mockMvc.perform(
                        get(PROCESSING_QUEUE_ENDPOINT)
                                .param(
                                        "stage",
                                        "CARGO_VERIFICATION"
                                )
                                .param("page", "2")
                                .param("size", "10")
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.empty").value(true));

        verify(adminShipmentProcessingService)
                .getProcessingQueue(
                        ProcessingStage.CARGO_VERIFICATION,
                        2,
                        10
                );
    }

    @Test
    void shouldForbidClientFromProcessingQueue()
            throws Exception {

        mockMvc.perform(
                        get(PROCESSING_QUEUE_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENT"
                                        )
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminShipmentProcessingService);
    }

    @Test
    void shouldRejectAnonymousProcessingQueueRequest()
            throws Exception {

        mockMvc.perform(
                        get(PROCESSING_QUEUE_ENDPOINT)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(adminShipmentProcessingService);
    }

    @Test
    void shouldAllowAdminToGetProcessingReadiness()
            throws Exception {

        ProcessingReadinessResponse response =
                ProcessingReadinessResponse.builder()
                        .shipmentId(10L)
                        .shipmentNumber("SHP-2026-00010")
                        .processingStage(
                                ProcessingStage.READY_FOR_EBILL
                        )
                        .containerReady(true)
                        .cargoReady(true)
                        .documentsReady(true)
                        .paymentReady(true)
                        .ebillReady(true)
                        .blockingReasons(List.of())
                        .build();

        when(adminShipmentProcessingService
                .getProcessingReadiness(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get(PROCESSING_READINESS_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(10))
                .andExpect(
                        jsonPath("$.shipmentNumber")
                                .value("SHP-2026-00010")
                )
                .andExpect(
                        jsonPath("$.processingStage")
                                .value("READY_FOR_EBILL")
                )
                .andExpect(
                        jsonPath("$.containerReady")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.cargoReady")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.documentsReady")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.paymentReady")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.ebillReady")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.blockingReasons")
                                .isEmpty()
                );

        verify(adminShipmentProcessingService)
                .getProcessingReadiness(10L);
    }
    @Test
    void shouldForbidClientFromProcessingReadiness()
            throws Exception {

        mockMvc.perform(
                        get(PROCESSING_READINESS_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENT"
                                        )
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                adminShipmentProcessingService
        );
    }
    @Test
    void shouldRejectAnonymousProcessingReadinessRequest()
            throws Exception {

        mockMvc.perform(
                        get(PROCESSING_READINESS_ENDPOINT)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(
                adminShipmentProcessingService
        );
    }
    @Test
    void shouldAllowAdminToGetEbillPreview()
            throws Exception {
        EbillPreviewResponse response =
                EbillPreviewResponse.builder()
                        .shipment(
                                new EbillShipmentSnapshot(
                                        10L,
                                        "SHP-2026-00010",
                                        100L,
                                        "Mumbai",
                                        "Pune",
                                        null,
                                        null,
                                        null,
                                        null,
                                        ProcessingStage.READY_FOR_EBILL,
                                        null,
                                        null,
                                        null
                                )
                        )
                        .client(
                                new EbillClientSnapshot(
                                        100L,
                                        "Cargo Client",
                                        "client@cargosphere.com",
                                        "9876543210",
                                        "ROLE_CLIENT",
                                        "ACTIVE",
                                        null,
                                        null
                                )
                        )
                        .originalCargo(List.of())
                        .confirmedCargo(List.of())
                        .containerAllocations(List.of())
                        .documents(List.of())
                        .payments(List.of())
                        .shipmentEvents(List.of())
                        .readiness(
                                new EbillReadinessSnapshot(
                                        ProcessingStage.READY_FOR_EBILL,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        List.of()
                                )
                        )
                        .build();

        when(adminShipmentProcessingService
                .getEbillPreview(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get(EBILL_PREVIEW_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.shipment.shipmentId")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.shipment.shipmentNumber")
                                .value("SHP-2026-00010")
                )
                .andExpect(
                        jsonPath("$.client.userId")
                                .value(100)
                )
                .andExpect(
                        jsonPath("$.client.fullName")
                                .value("Cargo Client")
                )
                .andExpect(
                        jsonPath("$.originalCargo")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$.readiness.ebillReady")
                                .value(true)
                );

        verify(adminShipmentProcessingService)
                .getEbillPreview(10L);
    }

    @Test
    void shouldForbidClientFromEbillPreview()
            throws Exception {
        mockMvc.perform(
                        get(EBILL_PREVIEW_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENT"
                                        )
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                adminShipmentProcessingService
        );
    }

    @Test
    void shouldRejectAnonymousEbillPreviewRequest()
            throws Exception {
        mockMvc.perform(
                        get(EBILL_PREVIEW_ENDPOINT)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(
                adminShipmentProcessingService
        );
    }

    @Test
    void shouldAllowAdminToContinueProcessing()
            throws Exception {

        ProcessingContinueResponse response =
                ProcessingContinueResponse.builder()
                        .shipmentId(10L)
                        .shipmentNumber("SHP-2026-00010")
                        .previousStage(
                                ProcessingStage.DOCUMENT_VERIFICATION
                        )
                        .processingStage(
                                ProcessingStage.PAYMENT_CONFIRMATION
                        )
                        .advancedAt(
                                OffsetDateTime.of(
                                        2026,
                                        8,
                                        2,
                                        10,
                                        30,
                                        0,
                                        0,
                                        ZoneOffset.UTC
                                )
                        )
                        .build();

        when(adminShipmentProcessingService
                .continueProcessing(10L))
                .thenReturn(response);

        mockMvc.perform(
                        post(PROCESSING_CONTINUE_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(10))
                .andExpect(
                        jsonPath("$.shipmentNumber")
                                .value("SHP-2026-00010")
                )
                .andExpect(
                        jsonPath("$.previousStage")
                                .value("DOCUMENT_VERIFICATION")
                )
                .andExpect(
                        jsonPath("$.processingStage")
                                .value("PAYMENT_CONFIRMATION")
                )
                .andExpect(
                        jsonPath("$.advancedAt")
                                .exists()
                );

        verify(adminShipmentProcessingService)
                .continueProcessing(10L);
    }
    @Test
    void shouldForbidClientFromContinuingProcessing()
            throws Exception {

        mockMvc.perform(
                        post(PROCESSING_CONTINUE_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENT"
                                        )
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                adminShipmentProcessingService
        );
    }
    @Test
    void shouldRejectAnonymousContinueProcessingRequest()
            throws Exception {

        mockMvc.perform(
                        post(PROCESSING_CONTINUE_ENDPOINT)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(
                adminShipmentProcessingService
        );
    }
    @Test
    void shouldReturnBadRequestWhenProcessingCannotContinue()
            throws Exception {

        when(adminShipmentProcessingService
                .continueProcessing(10L))
                .thenThrow(
                        new InvalidShipmentOperationException(
                                "Shipment 10 cannot continue from "
                                        + "processing stage READY_FOR_EBILL"
                        )
                );

        mockMvc.perform(
                        post(PROCESSING_CONTINUE_ENDPOINT)
                                .with(jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Shipment 10 cannot continue from "
                                                + "processing stage READY_FOR_EBILL"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(PROCESSING_CONTINUE_ENDPOINT)
                );

        verify(adminShipmentProcessingService)
                .continueProcessing(10L);
    }
    private CargoVerificationRequest
    createValidCargoVerificationRequest() {

        return CargoVerificationRequest.builder()
                .action(CargoVerificationAction.SAVE_DRAFT)
                .items(List.of(
                        CargoVerificationItemRequest.builder()
                                .cargoDetailId(11L)
                                .confirmedCargoName(
                                        "Electronics Box"
                                )
                                .build()
                ))
                .build();
    }
}
