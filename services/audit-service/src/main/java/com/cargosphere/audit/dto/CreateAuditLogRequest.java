package com.cargosphere.audit.dto;

import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditLogRequest {

    @Positive(message = "Actor user ID must be positive")
    private Long actorUserId;

    @Size(
            max = 50,
            message = "Actor role cannot exceed 50 characters"
    )
    private String actorRole;

    @NotNull(message = "Audit action is required")
    private AuditAction action;

    @NotNull(message = "Entity type is required")
    private AuditEntityType entityType;

    @Size(
            max = 100,
            message = "Entity ID cannot exceed 100 characters"
    )
    private String entityId;

    @NotBlank(message = "Service name is required")
    @Size(
            max = 100,
            message = "Service name cannot exceed 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "Service name can contain only letters, numbers and hyphens"
    )
    private String serviceName;

    @NotBlank(message = "Description is required")
    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    private AuditOutcome outcome;

    @Size(
            max = 100,
            message = "Request ID cannot exceed 100 characters"
    )
    private String requestId;

    @Size(
            max = 45,
            message = "IP address cannot exceed 45 characters"
    )
    private String ipAddress;

    @Size(
            max = 10,
            message = "HTTP method cannot exceed 10 characters"
    )
    @Pattern(
            regexp = "^(?i)(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)$",
            message = "HTTP method is invalid"
    )
    private String httpMethod;

    @Size(
            max = 255,
            message = "Endpoint cannot exceed 255 characters"
    )
    private String endpoint;

    @Min(
            value = 100,
            message = "Status code must be at least 100"
    )
    @Max(
            value = 599,
            message = "Status code cannot exceed 599"
    )
    private Integer statusCode;
}