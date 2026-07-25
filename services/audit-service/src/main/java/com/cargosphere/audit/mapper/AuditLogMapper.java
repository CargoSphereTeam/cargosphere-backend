package com.cargosphere.audit.mapper;

import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.entity.AuditLog;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AuditLogMapper {

    public AuditLog toEntity(
            CreateAuditLogRequest request
    ) {
        return AuditLog.builder()
                .actorUserId(request.getActorUserId())
                .actorRole(
                        normalizeUppercase(
                                request.getActorRole()
                        )
                )
                .action(request.getAction())
                .entityType(request.getEntityType())
                .entityId(
                        normalizeNullable(
                                request.getEntityId()
                        )
                )
                .serviceName(
                        normalizeServiceName(
                                request.getServiceName()
                        )
                )
                .description(
                        request.getDescription().trim()
                )
                .outcome(
                        request.getOutcome() == null
                                ? AuditOutcome.SUCCESS
                                : request.getOutcome()
                )
                .requestId(
                        normalizeNullable(
                                request.getRequestId()
                        )
                )
                .ipAddress(
                        normalizeNullable(
                                request.getIpAddress()
                        )
                )
                .httpMethod(
                        normalizeUppercase(
                                request.getHttpMethod()
                        )
                )
                .endpoint(
                        normalizeNullable(
                                request.getEndpoint()
                        )
                )
                .statusCode(request.getStatusCode())
                .build();
    }

    public AuditLogResponse toResponse(
            AuditLog auditLog
    ) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .actorUserId(
                        auditLog.getActorUserId()
                )
                .actorRole(auditLog.getActorRole())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .serviceName(auditLog.getServiceName())
                .description(auditLog.getDescription())
                .outcome(auditLog.getOutcome())
                .requestId(auditLog.getRequestId())
                .ipAddress(auditLog.getIpAddress())
                .httpMethod(auditLog.getHttpMethod())
                .endpoint(auditLog.getEndpoint())
                .statusCode(auditLog.getStatusCode())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private String normalizeServiceName(
            String serviceName
    ) {
        return serviceName
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeUppercase(String value) {
        String normalized = normalizeNullable(value);

        if (normalized == null) {
            return null;
        }

        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}