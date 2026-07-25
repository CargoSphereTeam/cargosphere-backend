package com.cargosphere.audit.service.impl;

import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.entity.AuditLog;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import com.cargosphere.audit.exception.AuditLogNotFoundException;
import com.cargosphere.audit.exception.InvalidAuditQueryException;
import com.cargosphere.audit.mapper.AuditLogMapper;
import com.cargosphere.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void createAuditLogShouldSaveAndReturnResponse() {
        CreateAuditLogRequest request =
                createRequest();

        AuditLog auditLog =
                auditLog();

        AuditLogResponse expectedResponse =
                auditResponse();

        when(auditLogMapper.toEntity(request))
                .thenReturn(auditLog);

        when(auditLogRepository.save(auditLog))
                .thenReturn(auditLog);

        when(auditLogMapper.toResponse(auditLog))
                .thenReturn(expectedResponse);

        AuditLogResponse actualResponse =
                auditLogService.createAuditLog(
                        request
                );

        assertSame(
                expectedResponse,
                actualResponse
        );

        verify(auditLogMapper).toEntity(request);
        verify(auditLogRepository).save(auditLog);
        verify(auditLogMapper).toResponse(auditLog);
    }

    @Test
    void getAuditLogByIdShouldReturnResponseWhenFound() {
        AuditLog auditLog = auditLog();

        AuditLogResponse expectedResponse =
                auditResponse();

        when(auditLogRepository.findById(1L))
                .thenReturn(Optional.of(auditLog));

        when(auditLogMapper.toResponse(auditLog))
                .thenReturn(expectedResponse);

        AuditLogResponse actualResponse =
                auditLogService.getAuditLogById(1L);

        assertSame(
                expectedResponse,
                actualResponse
        );
    }

    @Test
    void getAuditLogByIdShouldThrowWhenNotFound() {
        when(auditLogRepository.findById(99L))
                .thenReturn(Optional.empty());

        AuditLogNotFoundException exception =
                assertThrows(
                        AuditLogNotFoundException.class,
                        () -> auditLogService
                                .getAuditLogById(99L)
                );

        assertEquals(
                "Audit log not found with ID: 99",
                exception.getMessage()
        );
    }

    @Test
    void getAllAuditLogsShouldReturnMappedPage() {
        Pageable pageable =
                PageRequest.of(0, 20);

        AuditLog auditLog = auditLog();

        AuditLogResponse response =
                auditResponse();

        Page<AuditLog> entityPage =
                new PageImpl<>(
                        List.of(auditLog),
                        pageable,
                        1
                );

        when(auditLogRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                ))
                .thenReturn(entityPage);

        when(auditLogMapper.toResponse(auditLog))
                .thenReturn(response);

        Page<AuditLogResponse> result =
                auditLogService.getAllAuditLogs(
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                1,
                result.getContent().size()
        );
        assertSame(
                response,
                result.getContent().getFirst()
        );
    }

    @Test
    void getAllAuditLogsShouldRejectPageSizeAboveMaximum() {
        Pageable pageable =
                PageRequest.of(0, 101);

        InvalidAuditQueryException exception =
                assertThrows(
                        InvalidAuditQueryException.class,
                        () -> auditLogService
                                .getAllAuditLogs(pageable)
                );

        assertEquals(
                "Page size cannot exceed 100",
                exception.getMessage()
        );

        verify(
                auditLogRepository,
                never()
        ).findAllByOrderByCreatedAtDesc(
                any(Pageable.class)
        );
    }

    @Test
    void getAuditLogsByServiceNameShouldNormaliseName() {
        Pageable pageable =
                PageRequest.of(0, 20);

        Page<AuditLog> emptyPage =
                Page.empty(pageable);

        when(auditLogRepository
                .findByServiceNameOrderByCreatedAtDesc(
                        eq("payment-service"),
                        eq(pageable)
                ))
                .thenReturn(emptyPage);

        Page<AuditLogResponse> result =
                auditLogService
                        .getAuditLogsByServiceName(
                                " Payment-Service ",
                                pageable
                        );

        assertEquals(
                0,
                result.getTotalElements()
        );

        verify(auditLogRepository)
                .findByServiceNameOrderByCreatedAtDesc(
                        "payment-service",
                        pageable
                );
    }

    @Test
    void getAuditLogsByEntityShouldTrimEntityId() {
        Pageable pageable =
                PageRequest.of(0, 20);

        Page<AuditLog> emptyPage =
                Page.empty(pageable);

        when(auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        AuditEntityType.PAYMENT,
                        "101",
                        pageable
                ))
                .thenReturn(emptyPage);

        auditLogService.getAuditLogsByEntity(
                AuditEntityType.PAYMENT,
                " 101 ",
                pageable
        );

        verify(auditLogRepository)
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        AuditEntityType.PAYMENT,
                        "101",
                        pageable
                );
    }

    @Test
    void getAuditLogsByActorShouldRejectInvalidActorId() {
        Pageable pageable =
                PageRequest.of(0, 20);

        InvalidAuditQueryException exception =
                assertThrows(
                        InvalidAuditQueryException.class,
                        () -> auditLogService
                                .getAuditLogsByActorUserId(
                                        0L,
                                        pageable
                                )
                );

        assertEquals(
                "Actor user ID must be positive",
                exception.getMessage()
        );
    }

    private CreateAuditLogRequest createRequest() {
        return CreateAuditLogRequest.builder()
                .actorUserId(10L)
                .actorRole("ROLE_ADMIN")
                .action(AuditAction.PAYMENT_CREATED)
                .entityType(AuditEntityType.PAYMENT)
                .entityId("101")
                .serviceName("payment-service")
                .description("Payment created")
                .outcome(AuditOutcome.SUCCESS)
                .requestId("REQ-1001")
                .httpMethod("POST")
                .endpoint("/api/payments")
                .statusCode(201)
                .build();
    }

    private AuditLog auditLog() {
        return AuditLog.builder()
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
                .httpMethod("POST")
                .endpoint("/api/payments")
                .statusCode(201)
                .build();
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
                .httpMethod("POST")
                .endpoint("/api/payments")
                .statusCode(201)
                .build();
    }
}