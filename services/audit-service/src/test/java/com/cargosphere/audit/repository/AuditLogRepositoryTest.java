package com.cargosphere.audit.repository;

import com.cargosphere.audit.entity.AuditLog;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveShouldPersistAuditLog() {
        AuditLog auditLog = createAuditLog(
                101L,
                AuditAction.PAYMENT_CREATED,
                AuditEntityType.PAYMENT,
                "PAY-1001",
                "payment-service",
                "REQ-SAVE-1001",
                LocalDateTime.of(
                        2026,
                        7,
                        25,
                        10,
                        30
                )
        );

        AuditLog savedAuditLog =
                auditLogRepository.saveAndFlush(
                        auditLog
                );

        assertNotNull(savedAuditLog.getId());

        assertEquals(
                101L,
                savedAuditLog.getActorUserId()
        );

        assertEquals(
                "ROLE_ADMIN",
                savedAuditLog.getActorRole()
        );

        assertEquals(
                AuditAction.PAYMENT_CREATED,
                savedAuditLog.getAction()
        );

        assertEquals(
                AuditEntityType.PAYMENT,
                savedAuditLog.getEntityType()
        );

        assertEquals(
                "PAY-1001",
                savedAuditLog.getEntityId()
        );

        assertEquals(
                "payment-service",
                savedAuditLog.getServiceName()
        );

        assertEquals(
                AuditOutcome.SUCCESS,
                savedAuditLog.getOutcome()
        );

        assertEquals(
                "POST",
                savedAuditLog.getHttpMethod()
        );

        assertEquals(
                201,
                savedAuditLog.getStatusCode()
        );

        assertNotNull(savedAuditLog.getCreatedAt());
    }

    @Test
    void findAllShouldReturnNewestRecordsFirst() {
        AuditLog oldLog =
                auditLogRepository.save(
                        createAuditLog(
                                101L,
                                AuditAction.PAYMENT_CREATED,
                                AuditEntityType.PAYMENT,
                                "PAY-OLD",
                                "payment-service",
                                "REQ-ORDER-OLD",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        25,
                                        10,
                                        0
                                )
                        )
                );

        AuditLog newLog =
                auditLogRepository.save(
                        createAuditLog(
                                101L,
                                AuditAction.PAYMENT_REFUNDED,
                                AuditEntityType.PAYMENT,
                                "PAY-NEW",
                                "payment-service",
                                "REQ-ORDER-NEW",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        25,
                                        11,
                                        0
                                )
                        )
                );

        auditLogRepository.flush();

        Page<AuditLog> newResult =
                auditLogRepository
                        .findByRequestIdOrderByCreatedAtDesc(
                                "REQ-ORDER-NEW",
                                PageRequest.of(0, 10)
                        );

        Page<AuditLog> oldResult =
                auditLogRepository
                        .findByRequestIdOrderByCreatedAtDesc(
                                "REQ-ORDER-OLD",
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, newResult.getTotalElements());
        assertEquals(1, oldResult.getTotalElements());

        assertEquals(
                newLog.getId(),
                newResult.getContent()
                        .get(0)
                        .getId()
        );

        assertEquals(
                oldLog.getId(),
                oldResult.getContent()
                        .get(0)
                        .getId()
        );

        assertTrue(
                newResult.getContent()
                        .get(0)
                        .getCreatedAt()
                        .isAfter(
                                oldResult.getContent()
                                        .get(0)
                                        .getCreatedAt()
                        )
        );
    }

    @Test
    void findByActorUserIdShouldReturnMatchingLogs() {
        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.PAYMENT_CREATED,
                        AuditEntityType.PAYMENT,
                        "PAY-ACTOR-1",
                        "payment-service",
                        "REQ-ACTOR-1",
                        LocalDateTime.now()
                )
        );

        auditLogRepository.save(
                createAuditLog(
                        202L,
                        AuditAction.SHIPMENT_CREATED,
                        AuditEntityType.SHIPMENT,
                        "SHIP-ACTOR-2",
                        "shipment-service",
                        "REQ-ACTOR-2",
                        LocalDateTime.now()
                                .plusSeconds(1)
                )
        );

        auditLogRepository.flush();

        Page<AuditLog> result =
                auditLogRepository
                        .findByActorUserIdOrderByCreatedAtDesc(
                                101L,
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                101L,
                result.getContent()
                        .get(0)
                        .getActorUserId()
        );
    }

    @Test
    void findByActionShouldReturnMatchingLogs() {
        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.PAYMENT_CREATED,
                        AuditEntityType.PAYMENT,
                        "PAY-ACTION-1",
                        "payment-service",
                        "REQ-ACTION-1",
                        LocalDateTime.now()
                )
        );

        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.PAYMENT_REFUNDED,
                        AuditEntityType.PAYMENT,
                        "PAY-ACTION-2",
                        "payment-service",
                        "REQ-ACTION-2",
                        LocalDateTime.now()
                                .plusSeconds(1)
                )
        );

        auditLogRepository.flush();

        Page<AuditLog> result =
                auditLogRepository
                        .findByActionOrderByCreatedAtDesc(
                                AuditAction.PAYMENT_REFUNDED,
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                AuditAction.PAYMENT_REFUNDED,
                result.getContent()
                        .get(0)
                        .getAction()
        );
    }

    @Test
    void findByEntityShouldReturnMatchingLogs() {
        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.DOCUMENT_CREATED,
                        AuditEntityType.DOCUMENT,
                        "DOC-1001",
                        "document-service",
                        "REQ-ENTITY-1",
                        LocalDateTime.now()
                )
        );

        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.PAYMENT_CREATED,
                        AuditEntityType.PAYMENT,
                        "PAY-1001",
                        "payment-service",
                        "REQ-ENTITY-2",
                        LocalDateTime.now()
                                .plusSeconds(1)
                )
        );

        auditLogRepository.flush();

        Page<AuditLog> result =
                auditLogRepository
                        .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                                AuditEntityType.DOCUMENT,
                                "DOC-1001",
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                AuditEntityType.DOCUMENT,
                result.getContent()
                        .get(0)
                        .getEntityType()
        );

        assertEquals(
                "DOC-1001",
                result.getContent()
                        .get(0)
                        .getEntityId()
        );
    }

    @Test
    void findByServiceNameShouldReturnMatchingLogs() {
        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.PAYMENT_CREATED,
                        AuditEntityType.PAYMENT,
                        "PAY-SERVICE-1",
                        "payment-service",
                        "REQ-SERVICE-1",
                        LocalDateTime.now()
                )
        );

        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.SHIPMENT_CREATED,
                        AuditEntityType.SHIPMENT,
                        "SHIP-SERVICE-2",
                        "shipment-service",
                        "REQ-SERVICE-2",
                        LocalDateTime.now()
                                .plusSeconds(1)
                )
        );

        auditLogRepository.flush();

        Page<AuditLog> result =
                auditLogRepository
                        .findByServiceNameOrderByCreatedAtDesc(
                                "payment-service",
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "payment-service",
                result.getContent()
                        .get(0)
                        .getServiceName()
        );
    }

    @Test
    void findByRequestIdShouldReturnMatchingLogs() {
        auditLogRepository.save(
                createAuditLog(
                        101L,
                        AuditAction.PAYMENT_CREATED,
                        AuditEntityType.PAYMENT,
                        "PAY-REQUEST-1",
                        "payment-service",
                        "REQ-UNIQUE-1001",
                        LocalDateTime.now()
                )
        );

        auditLogRepository.flush();

        Page<AuditLog> result =
                auditLogRepository
                        .findByRequestIdOrderByCreatedAtDesc(
                                "REQ-UNIQUE-1001",
                                PageRequest.of(0, 10)
                        );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "REQ-UNIQUE-1001",
                result.getContent()
                        .get(0)
                        .getRequestId()
        );
    }

    @Test
    void databaseShouldRejectAuditLogUpdate() {
        AuditLog savedAuditLog =
                auditLogRepository.saveAndFlush(
                        createAuditLog(
                                101L,
                                AuditAction.PAYMENT_CREATED,
                                AuditEntityType.PAYMENT,
                                "PAY-UPDATE-BLOCKED",
                                "payment-service",
                                "REQ-UPDATE-BLOCKED",
                                LocalDateTime.now()
                        )
                );

        assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.update(
                        """
                        UPDATE audit_schema.audit_logs
                        SET description = ?
                        WHERE id = ?
                        """,
                        "Modified audit description",
                        savedAuditLog.getId()
                )
        );
    }

    @Test
    void databaseShouldRejectAuditLogDelete() {
        AuditLog savedAuditLog =
                auditLogRepository.saveAndFlush(
                        createAuditLog(
                                101L,
                                AuditAction.PAYMENT_CREATED,
                                AuditEntityType.PAYMENT,
                                "PAY-DELETE-BLOCKED",
                                "payment-service",
                                "REQ-DELETE-BLOCKED",
                                LocalDateTime.now()
                        )
                );

        assertThrows(
                DataAccessException.class,
                () -> jdbcTemplate.update(
                        """
                        DELETE FROM audit_schema.audit_logs
                        WHERE id = ?
                        """,
                        savedAuditLog.getId()
                )
        );
    }

    private AuditLog createAuditLog(
            Long actorUserId,
            AuditAction action,
            AuditEntityType entityType,
            String entityId,
            String serviceName,
            String requestId,
            LocalDateTime createdAt
    ) {
        return AuditLog.builder()
                .actorUserId(actorUserId)
                .actorRole("ROLE_ADMIN")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .serviceName(serviceName)
                .description(
                        "Repository integration test"
                )
                .outcome(AuditOutcome.SUCCESS)
                .requestId(requestId)
                .ipAddress("127.0.0.1")
                .httpMethod("POST")
                .endpoint("/api/test")
                .statusCode(201)
                .createdAt(createdAt)
                .build();
    }
}