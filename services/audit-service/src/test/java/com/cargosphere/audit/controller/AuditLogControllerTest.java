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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuditLogController.class,
        AuditHealthController.class
})
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void healthShouldRemainPublic() throws Exception {
        mockMvc.perform(
                        get("/api/audits/health")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.service")
                                .value("audit-service")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                );

        verifyNoInteractions(auditLogService);
    }

    @Test
    void anonymousUserShouldReceiveUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/audits"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(auditLogService);
    }

    @Test
    void clientShouldReceiveForbidden()
            throws Exception {

        mockMvc.perform(
                        get("/api/audits")
                                .with(clientJwt())
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Access denied")
                );

        verifyNoInteractions(auditLogService);
    }

    @Test
    void adminShouldCreateAuditLog()
            throws Exception {

        AuditLogResponse response =
                auditResponse();

        when(auditLogService.createAuditLog(
                any(CreateAuditLogRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/audits")
                                .with(adminJwt())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "actorUserId": 10,
                                          "actorRole": "ROLE_ADMIN",
                                          "action": "PAYMENT_CREATED",
                                          "entityType": "PAYMENT",
                                          "entityId": "101",
                                          "serviceName": "payment-service",
                                          "description": "Payment created",
                                          "outcome": "SUCCESS",
                                          "requestId": "REQ-1001",
                                          "ipAddress": "127.0.0.1",
                                          "httpMethod": "POST",
                                          "endpoint": "/api/payments",
                                          "statusCode": 201
                                        }
                                        """)
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
                )
                .andExpect(
                        jsonPath("$.entityType")
                                .value("PAYMENT")
                );

        verify(auditLogService)
                .createAuditLog(
                        any(CreateAuditLogRequest.class)
                );
    }

    @Test
    void createAuditLogShouldRejectInvalidBody()
            throws Exception {

        mockMvc.perform(
                        post("/api/audits")
                                .with(adminJwt())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(
                                "$.validationErrors.action"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.entityType"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.serviceName"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.description"
                        ).exists()
                );

        verifyNoInteractions(auditLogService);
    }

    @Test
    void adminShouldGetAllAuditLogs()
            throws Exception {

        Page<AuditLogResponse> page =
                new PageImpl<>(
                        List.of(auditResponse()),
                        PageRequest.of(0, 20),
                        1
                );

        when(auditLogService.getAllAuditLogs(
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/audits")
                                .with(adminJwt())
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.page").value(0)
                )
                .andExpect(
                        jsonPath("$.size").value(20)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                );

        verify(auditLogService)
                .getAllAuditLogs(any());
    }

    @Test
    void adminShouldFilterByAction()
            throws Exception {

        Page<AuditLogResponse> page =
                new PageImpl<>(
                        List.of(auditResponse()),
                        PageRequest.of(0, 20),
                        1
                );

        when(auditLogService.getAuditLogsByAction(
                eq(AuditAction.PAYMENT_CREATED),
                any()
        )).thenReturn(page);

        mockMvc.perform(
                        get(
                                "/api/audits/action/PAYMENT_CREATED"
                        )
                                .with(adminJwt())
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].action"
                        ).value("PAYMENT_CREATED")
                );

        verify(auditLogService)
                .getAuditLogsByAction(
                        eq(AuditAction.PAYMENT_CREATED),
                        any()
                );
    }

    @Test
    void pageSizeAboveMaximumShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/audits")
                                .with(adminJwt())
                                .param("page", "0")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(auditLogService);
    }

    private RequestPostProcessor adminJwt() {
        return jwt()
                .jwt(builder ->
                        builder
                                .subject(
                                        "admin@cargosphere.com"
                                )
                                .claim("userId", 1L)
                                .claim(
                                        "authorities",
                                        List.of(
                                                "ROLE_ADMIN"
                                        )
                                )
                )
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMIN"
                        )
                );
    }

    private RequestPostProcessor clientJwt() {
        return jwt()
                .jwt(builder ->
                        builder
                                .subject(
                                        "client@example.com"
                                )
                                .claim("userId", 101L)
                                .claim(
                                        "authorities",
                                        List.of(
                                                "ROLE_CLIENT"
                                        )
                                )
                )
                .authorities(
                        new SimpleGrantedAuthority(
                                "ROLE_CLIENT"
                        )
                );
    }

    private AuditLogResponse auditResponse() {
        return AuditLogResponse.builder()
                .id(1L)
                .actorUserId(10L)
                .actorRole("ROLE_ADMIN")
                .action(AuditAction.PAYMENT_CREATED)
                .entityType(AuditEntityType.PAYMENT)
                .entityId("101")
                .serviceName("payment-service")
                .description("Payment created")
                .outcome(AuditOutcome.SUCCESS)
                .requestId("REQ-1001")
                .ipAddress("127.0.0.1")
                .httpMethod("POST")
                .endpoint("/api/payments")
                .statusCode(201)
                .createdAt(LocalDateTime.now())
                .build();
    }
}