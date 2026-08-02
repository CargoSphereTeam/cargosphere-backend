package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.config.SecurityConfig;
import com.cargosphere.shipment.dto.admin.CargoVerificationAction;
import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.exception.InvalidProcessingStageException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminShipmentProcessingController.class)
@Import(SecurityConfig.class)
class AdminShipmentProcessingControllerTest {

    private static final String ENDPOINT =
            "/api/admin/shipments/10/cargo-verification";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CargoVerificationService cargoVerificationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldAllowAdminToSaveCargoVerification() throws Exception {
        CargoVerificationRequest request =
                createValidRequest();

        CargoVerificationResponse response =
                CargoVerificationResponse.builder()
                        .shipmentId(10L)
                        .action(CargoVerificationAction.SAVE_DRAFT)
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
                        put(ENDPOINT)
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
    void shouldForbidClientFromCargoVerification() throws Exception {
        CargoVerificationRequest request =
                createValidRequest();

        mockMvc.perform(
                        put(ENDPOINT)
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
    void shouldRejectAnonymousUser() throws Exception {
        CargoVerificationRequest request =
                createValidRequest();

        mockMvc.perform(
                        put(ENDPOINT)
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
    void shouldRejectInvalidRequestBody() throws Exception {
        String invalidRequest = """
                {
                  "items": []
                }
                """;

        mockMvc.perform(
                        put(ENDPOINT)
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
    void shouldReturnConflictForIncorrectProcessingStage()
            throws Exception {

        CargoVerificationRequest request =
                createValidRequest();

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
                        put(ENDPOINT)
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

    private CargoVerificationRequest createValidRequest() {
        return CargoVerificationRequest.builder()
                .action(CargoVerificationAction.SAVE_DRAFT)
                .items(List.of(
                        com.cargosphere.shipment.dto.admin
                                .CargoVerificationItemRequest
                                .builder()
                                .cargoDetailId(11L)
                                .confirmedCargoName(
                                        "Electronics Box"
                                )
                                .build()
                ))
                .build();
    }
}