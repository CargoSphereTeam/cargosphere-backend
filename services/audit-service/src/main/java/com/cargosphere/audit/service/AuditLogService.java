package com.cargosphere.audit.service;

import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    AuditLogResponse createAuditLog(
            CreateAuditLogRequest request
    );

    AuditLogResponse getAuditLogById(
            Long auditLogId
    );

    Page<AuditLogResponse> getAllAuditLogs(
            Pageable pageable
    );

    Page<AuditLogResponse> getAuditLogsByActorUserId(
            Long actorUserId,
            Pageable pageable
    );

    Page<AuditLogResponse> getAuditLogsByAction(
            AuditAction action,
            Pageable pageable
    );

    Page<AuditLogResponse> getAuditLogsByEntity(
            AuditEntityType entityType,
            String entityId,
            Pageable pageable
    );

    Page<AuditLogResponse> getAuditLogsByServiceName(
            String serviceName,
            Pageable pageable
    );

    Page<AuditLogResponse> getAuditLogsByRequestId(
            String requestId,
            Pageable pageable
    );
}