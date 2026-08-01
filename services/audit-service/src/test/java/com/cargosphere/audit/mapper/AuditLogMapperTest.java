package com.cargosphere.audit.mapper;

import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.entity.AuditLog;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogMapperTest {

    private AuditLogMapper auditLogMapper;

    @BeforeEach
    void setUp() {
        auditLogMapper = new AuditLogMapper();
    }

    @Test
    void toEntityShouldMapAndNormaliseRequest() {
        CreateAuditLogRequest request =
                CreateAuditLogRequest.builder()
                        .actorUserId(10L)
                        .actorRole(" role_admin ")
                        .action(AuditAction.PAYMENT_CREATED)
                        .entityType(AuditEntityType.PAYMENT)
                        .entityId(" 101 ")
                        .serviceName(" Payment-Service ")
                        .description(
                                " Payment record created "
                        )
                        .outcome(AuditOutcome.SUCCESS)
                        .requestId(" REQ-1001 ")
                        .ipAddress(" 127.0.0.1 ")
                        .httpMethod(" post ")
                        .endpoint(" /api/payments ")
                        .statusCode(201)
                        .build();

        AuditLog auditLog =
                auditLogMapper.toEntity(request);

        assertEquals(
                10L,
                auditLog.getActorUserId()
        );

        assertEquals(
                "ROLE_ADMIN",
                auditLog.getActorRole()
        );

        assertEquals(
                AuditAction.PAYMENT_CREATED,
                auditLog.getAction()
        );

        assertEquals(
                AuditEntityType.PAYMENT,
                auditLog.getEntityType()
        );

        assertEquals(
                "101",
                auditLog.getEntityId()
        );

        assertEquals(
                "payment-service",
                auditLog.getServiceName()
        );

        assertEquals(
                "Payment record created",
                auditLog.getDescription()
        );

        assertEquals(
                AuditOutcome.SUCCESS,
                auditLog.getOutcome()
        );

        assertEquals(
                "REQ-1001",
                auditLog.getRequestId()
        );

        assertEquals(
                "127.0.0.1",
                auditLog.getIpAddress()
        );

        assertEquals(
                "POST",
                auditLog.getHttpMethod()
        );

        assertEquals(
                "/api/payments",
                auditLog.getEndpoint()
        );

        assertEquals(
                201,
                auditLog.getStatusCode()
        );
    }

    @Test
    void toResponseShouldMapAllFields() {
        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        7,
                        25,
                        10,
                        30
                );

        AuditLog auditLog =
                AuditLog.builder()
                        .id(1L)
                        .actorUserId(10L)
                        .actorRole("ROLE_ADMIN")
                        .action(
                                AuditAction.PAYMENT_CREATED
                        )
                        .entityType(
                                AuditEntityType.PAYMENT
                        )
                        .entityId("101")
                        .serviceName("payment-service")
                        .description(
                                "Payment record created"
                        )
                        .outcome(AuditOutcome.SUCCESS)
                        .requestId("REQ-1001")
                        .ipAddress("127.0.0.1")
                        .httpMethod("POST")
                        .endpoint("/api/payments")
                        .statusCode(201)
                        .createdAt(createdAt)
                        .build();

        AuditLogResponse response =
                auditLogMapper.toResponse(auditLog);

        assertEquals(1L, response.getId());
        assertEquals(
                10L,
                response.getActorUserId()
        );
        assertEquals(
                "ROLE_ADMIN",
                response.getActorRole()
        );
        assertEquals(
                AuditAction.PAYMENT_CREATED,
                response.getAction()
        );
        assertEquals(
                AuditEntityType.PAYMENT,
                response.getEntityType()
        );
        assertEquals(
                "payment-service",
                response.getServiceName()
        );
        assertEquals(
                AuditOutcome.SUCCESS,
                response.getOutcome()
        );
        assertEquals(
                "REQ-1001",
                response.getRequestId()
        );
        assertEquals(
                createdAt,
                response.getCreatedAt()
        );
    }
}