package com.cargosphere.audit.dto;

import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.entity.enums.AuditOutcome;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "CreateAuditLogRequest",
        description = "Request body used to create an audit log"
)
public class CreateAuditLogRequest {

    @Positive(message = "Actor user ID must be positive")
    @Schema(description = "Actor user ID", example = "10")
    private Long actorUserId;

    @Size(
            max = 50,
            message = "Actor role cannot exceed 50 characters"
    )
    @Schema(description = "Actor role", example = "ROLE_ADMIN")
    private String actorRole;

    @NotNull(message = "Audit action is required")
    @Schema(
            description = "Action performed",
            example = "CREATE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AuditAction action;

    @NotNull(message = "Entity type is required")
    @Schema(
            description = "Affected entity type",
            example = "SHIPMENT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AuditEntityType entityType;

    @Size(
            max = 100,
            message = "Entity ID cannot exceed 100 characters"
    )
    @Schema(description = "Affected entity ID", example = "1001")
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
    @Schema(
            description = "Originating service",
            example = "shipment-service",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String serviceName;

    @NotBlank(message = "Description is required")
    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    @Schema(
            description = "Human-readable audit description",
            example = "Shipment created successfully",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @Schema(description = "Audit outcome", example = "SUCCESS")
    private AuditOutcome outcome;

    @Size(
            max = 100,
            message = "Request ID cannot exceed 100 characters"
    )
    @Schema(description = "Distributed request ID", example = "req-123")
    private String requestId;

    @Size(
            max = 45,
            message = "IP address cannot exceed 45 characters"
    )
    @Schema(description = "Client IP address", example = "127.0.0.1")
    private String ipAddress;

    @Size(
            max = 10,
            message = "HTTP method cannot exceed 10 characters"
    )
    @Pattern(
            regexp = "^(?i)(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)$",
            message = "HTTP method is invalid"
    )
    @Schema(description = "HTTP method", example = "POST")
    private String httpMethod;

    @Size(
            max = 255,
            message = "Endpoint cannot exceed 255 characters"
    )
    @Schema(
            description = "Endpoint that produced the event",
            example = "/api/shipments"
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
    @Schema(description = "HTTP status code", example = "201")
    private Integer statusCode;
}