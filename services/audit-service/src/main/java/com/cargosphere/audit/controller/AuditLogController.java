package com.cargosphere.audit.controller;

import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.dto.PageResponse;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.service.AuditLogService;
import com.cargosphere.audit.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Audit Logs",
        description = "Audit-log creation, lookup and filtering"
)
public class AuditLogController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAXIMUM_PAGE_SIZE = 100;

    private final AuditLogService auditLogService;

    @Operation(
            summary = "Create an audit log",
            description = "Creates an audit log using ADMIN JWT authentication."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogResponse>
    createAuditLog(
            @Valid
            @RequestBody
            CreateAuditLogRequest request
    ) {
        AuditLogResponse response =
                auditLogService.createAuditLog(
                        request
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }


    @Operation(
            summary = "Create an internal audit log",
            description =
                    "Creates an audit log using the X-Internal-API-Key header."
    )
    @SecurityRequirement(
            name = OpenApiConfig.INTERNAL_API_KEY_SCHEME_NAME
    )
    @PostMapping("/internal")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<AuditLogResponse>
    createInternalAuditLog(
            @Valid
            @RequestBody
            CreateAuditLogRequest request
    ) {
        AuditLogResponse response =
                auditLogService.createAuditLog(
                        request
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/audits/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @Operation(
            summary = "Get audit log by ID",
            description = "Returns one audit log. Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping("/{auditLogId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditLogResponse>
    getAuditLogById(
            @PathVariable
            @Positive(
                    message = "Audit log ID must be positive"
            )
            Long auditLogId
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogById(
                        auditLogId
                )
        );
    }

    @Operation(
            summary = "Get all audit logs",
            description = "Returns paginated audit logs. Requires ROLE_ADMIN."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAllAuditLogs(
            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(
                    defaultValue = ""
                            + DEFAULT_PAGE_SIZE
            )
            @Min(
                    value = 1,
                    message = "Page size must be greater than zero"
            )
            @Max(
                    value = MAXIMUM_PAGE_SIZE,
                    message = "Page size cannot exceed 100"
            )
            int size
    ) {
        Page<AuditLogResponse> result =
                auditLogService.getAllAuditLogs(
                        createPageable(page, size)
                );

        return ResponseEntity.ok(
                PageResponse.from(result)
        );
    }

    @Operation(
            summary = "Get audit logs by actor",
            description = "Filters audit logs by actor user ID."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping("/actor/{actorUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAuditLogsByActorUserId(
            @PathVariable
            @Positive(
                    message = "Actor user ID must be positive"
            )
            Long actorUserId,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(
                    defaultValue = ""
                            + DEFAULT_PAGE_SIZE
            )
            @Min(
                    value = 1,
                    message = "Page size must be greater than zero"
            )
            @Max(
                    value = MAXIMUM_PAGE_SIZE,
                    message = "Page size cannot exceed 100"
            )
            int size
    ) {
        Page<AuditLogResponse> result =
                auditLogService
                        .getAuditLogsByActorUserId(
                                actorUserId,
                                createPageable(page, size)
                        );

        return ResponseEntity.ok(
                PageResponse.from(result)
        );
    }

    @Operation(
            summary = "Get audit logs by action",
            description = "Filters audit logs by audit action."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAuditLogsByAction(
            @PathVariable
            AuditAction action,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(
                    defaultValue = ""
                            + DEFAULT_PAGE_SIZE
            )
            @Min(
                    value = 1,
                    message = "Page size must be greater than zero"
            )
            @Max(
                    value = MAXIMUM_PAGE_SIZE,
                    message = "Page size cannot exceed 100"
            )
            int size
    ) {
        Page<AuditLogResponse> result =
                auditLogService.getAuditLogsByAction(
                        action,
                        createPageable(page, size)
                );

        return ResponseEntity.ok(
                PageResponse.from(result)
        );
    }

    @Operation(
            summary = "Get audit logs by entity",
            description = "Filters audit logs by entity type and entity ID."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping("/entity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAuditLogsByEntity(
            @RequestParam
            AuditEntityType entityType,

            @RequestParam
            @NotBlank(
                    message = "Entity ID is required"
            )
            @Size(
                    max = 100,
                    message = "Entity ID cannot exceed 100 characters"
            )
            String entityId,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(
                    defaultValue = ""
                            + DEFAULT_PAGE_SIZE
            )
            @Min(
                    value = 1,
                    message = "Page size must be greater than zero"
            )
            @Max(
                    value = MAXIMUM_PAGE_SIZE,
                    message = "Page size cannot exceed 100"
            )
            int size
    ) {
        Page<AuditLogResponse> result =
                auditLogService.getAuditLogsByEntity(
                        entityType,
                        entityId,
                        createPageable(page, size)
                );

        return ResponseEntity.ok(
                PageResponse.from(result)
        );
    }

    @Operation(
            summary = "Get audit logs by service",
            description = "Filters audit logs by service name."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping("/service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAuditLogsByServiceName(
            @RequestParam
            @NotBlank(
                    message = "Service name is required"
            )
            @Size(
                    max = 100,
                    message = "Service name cannot exceed 100 characters"
            )
            String serviceName,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(
                    defaultValue = ""
                            + DEFAULT_PAGE_SIZE
            )
            @Min(
                    value = 1,
                    message = "Page size must be greater than zero"
            )
            @Max(
                    value = MAXIMUM_PAGE_SIZE,
                    message = "Page size cannot exceed 100"
            )
            int size
    ) {
        Page<AuditLogResponse> result =
                auditLogService
                        .getAuditLogsByServiceName(
                                serviceName,
                                createPageable(page, size)
                        );

        return ResponseEntity.ok(
                PageResponse.from(result)
        );
    }

    @Operation(
            summary = "Get audit logs by request ID",
            description = "Filters audit logs by request ID."
    )
    @SecurityRequirement(name = OpenApiConfig.JWT_SCHEME_NAME)
    @GetMapping("/request")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAuditLogsByRequestId(
            @RequestParam
            @NotBlank(
                    message = "Request ID is required"
            )
            @Size(
                    max = 100,
                    message = "Request ID cannot exceed 100 characters"
            )
            String requestId,

            @RequestParam(defaultValue = "0")
            @Min(
                    value = 0,
                    message = "Page number cannot be negative"
            )
            int page,

            @RequestParam(
                    defaultValue = ""
                            + DEFAULT_PAGE_SIZE
            )
            @Min(
                    value = 1,
                    message = "Page size must be greater than zero"
            )
            @Max(
                    value = MAXIMUM_PAGE_SIZE,
                    message = "Page size cannot exceed 100"
            )
            int size
    ) {
        Page<AuditLogResponse> result =
                auditLogService.getAuditLogsByRequestId(
                        requestId,
                        createPageable(page, size)
                );

        return ResponseEntity.ok(
                PageResponse.from(result)
        );
    }

    private Pageable createPageable(
            int page,
            int size
    ) {
        return PageRequest.of(page, size);
    }
}