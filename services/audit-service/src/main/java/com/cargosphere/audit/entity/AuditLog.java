package com.cargosphere.audit.entity;

import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_logs",
        schema = "audit_schema",
        indexes = {
                @Index(
                        name = "idx_audit_logs_actor_user_id",
                        columnList = "actor_user_id"
                ),
                @Index(
                        name = "idx_audit_logs_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_audit_logs_entity",
                        columnList = "entity_type, entity_id"
                ),
                @Index(
                        name = "idx_audit_logs_service_name",
                        columnList = "service_name"
                ),
                @Index(
                        name = "idx_audit_logs_request_id",
                        columnList = "request_id"
                ),
                @Index(
                        name = "idx_audit_logs_created_at",
                        columnList = "created_at"
                )
        }
)
@Immutable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "actor_user_id",
            updatable = false
    )
    private Long actorUserId;

    @Column(
            name = "actor_role",
            length = 50,
            updatable = false
    )
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 80,
            updatable = false
    )
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "entity_type",
            nullable = false,
            length = 50,
            updatable = false
    )
    private AuditEntityType entityType;

    @Column(
            name = "entity_id",
            length = 100,
            updatable = false
    )
    private String entityId;

    @Column(
            name = "service_name",
            nullable = false,
            length = 100,
            updatable = false
    )
    private String serviceName;

    @Column(
            name = "description",
            nullable = false,
            length = 500,
            updatable = false
    )
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "outcome",
            nullable = false,
            length = 20,
            updatable = false
    )
    private AuditOutcome outcome =
            AuditOutcome.SUCCESS;

    @Column(
            name = "request_id",
            length = 100,
            updatable = false
    )
    private String requestId;

    @Column(
            name = "ip_address",
            length = 45,
            updatable = false
    )
    private String ipAddress;

    @Column(
            name = "http_method",
            length = 10,
            updatable = false
    )
    private String httpMethod;

    @Column(
            name = "endpoint",
            length = 255,
            updatable = false
    )
    private String endpoint;

    @Column(
            name = "status_code",
            updatable = false
    )
    private Integer statusCode;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (outcome == null) {
            outcome = AuditOutcome.SUCCESS;
        }

        actorRole = normalizeNullable(actorRole);
        entityId = normalizeNullable(entityId);
        requestId = normalizeNullable(requestId);
        ipAddress = normalizeNullable(ipAddress);
        httpMethod = normalizeUppercase(httpMethod);
        endpoint = normalizeNullable(endpoint);

        if (serviceName != null) {
            serviceName = serviceName.trim();
        }

        if (description != null) {
            description = description.trim();
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeUppercase(String value) {
        String normalized = normalizeNullable(value);

        if (normalized == null) {
            return null;
        }

        return normalized.toUpperCase();
    }
}