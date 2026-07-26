package com.cargosphere.audit.controller;

import com.cargosphere.audit.config.SecurityConfig;
import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import com.cargosphere.audit.exception.GlobalExceptionHandler;
import com.cargosphere.audit.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "app.internal-audit.api-key=test-internal-audit-key-0123456789abcdef"
})
class InternalAuditEndpointTest {

    private static final String INTERNAL_KEY =
            "test-internal-audit-key-0123456789abcdef";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void missingInternalKeyShouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        post("/api/audits/internal")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validRequestBody())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid or missing internal audit API key"
                                )
                );

        verifyNoInteractions(auditLogService);
    }

    @Test
    void invalidInternalKeyShouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                        post("/api/audits/internal")
                                .header(
                                        "X-Internal-API-Key",
                                        "incorrect-internal-api-key-123456"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validRequestBody())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(auditLogService);
    }

    @Test
    void validInternalKeyShouldCreateAuditLog()
            throws Exception {

        AuditLogResponse response =
                AuditLogResponse.builder()
                        .id(1L)
                        .actorUserId(10L)
                        .actorRole("ROLE_CLIENT")
                        .action(
                                AuditAction.PAYMENT_CREATED
                        )
                        .entityType(
                                AuditEntityType.PAYMENT
                        )
                        .entityId("101")
                        .serviceName("payment-service")
                        .description("Payment created")
                        .outcome(AuditOutcome.SUCCESS)
                        .requestId("REQ-1001")
                        .httpMethod("POST")
                        .endpoint("/api/payments")
                        .statusCode(201)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(auditLogService.createAuditLog(
                any(CreateAuditLogRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/audits/internal")
                                .header(
                                        "X-Internal-API-Key",
                                        INTERNAL_KEY
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validRequestBody())
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "http://localhost/api/audits/1"
                        )
                )
                .andExpect(
                        jsonPath("$.id").value(1)
                )
                .andExpect(
                        jsonPath("$.action")
                                .value("PAYMENT_CREATED")
                );

        verify(auditLogService)
                .createAuditLog(
                        any(CreateAuditLogRequest.class)
                );
    }

    private String validRequestBody() {
        return """
                {
                  "actorUserId": 10,
                  "actorRole": "ROLE_CLIENT",
                  "action": "PAYMENT_CREATED",
                  "entityType": "PAYMENT",
                  "entityId": "101",
                  "serviceName": "payment-service",
                  "description": "Payment created",
                  "outcome": "SUCCESS",
                  "requestId": "REQ-1001",
                  "httpMethod": "POST",
                  "endpoint": "/api/payments",
                  "statusCode": 201
                }
                """;
    }
}