package com.cargosphere.audit.dto;

import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;

    private Long actorUserId;

    private String actorRole;

    private AuditAction action;

    private AuditEntityType entityType;

    private String entityId;

    private String serviceName;

    private String description;

    private AuditOutcome outcome;

    private String requestId;

    private String ipAddress;

    private String httpMethod;

    private String endpoint;

    private Integer statusCode;

    private LocalDateTime createdAt;
}