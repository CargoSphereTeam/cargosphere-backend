package com.cargosphere.audit.service.impl;

import com.cargosphere.audit.dto.AuditLogResponse;
import com.cargosphere.audit.dto.CreateAuditLogRequest;
import com.cargosphere.audit.entity.AuditLog;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import com.cargosphere.audit.exception.AuditLogNotFoundException;
import com.cargosphere.audit.exception.InvalidAuditQueryException;
import com.cargosphere.audit.mapper.AuditLogMapper;
import com.cargosphere.audit.repository.AuditLogRepository;
import com.cargosphere.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl
        implements AuditLogService {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public AuditLogResponse createAuditLog(
            CreateAuditLogRequest request
    ) {
        AuditLog auditLog =
                auditLogMapper.toEntity(request);

        AuditLog savedAuditLog =
                auditLogRepository.save(auditLog);

        return auditLogMapper.toResponse(
                savedAuditLog
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(
            Long auditLogId
    ) {
        validatePositiveId(
                auditLogId,
                "Audit log ID"
        );

        return auditLogMapper.toResponse(
                findAuditLogById(auditLogId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(
            Pageable pageable
    ) {
        validatePageable(pageable);

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse>
    getAuditLogsByActorUserId(
            Long actorUserId,
            Pageable pageable
    ) {
        validatePositiveId(
                actorUserId,
                "Actor user ID"
        );

        validatePageable(pageable);

        return auditLogRepository
                .findByActorUserIdOrderByCreatedAtDesc(
                        actorUserId,
                        pageable
                )
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByAction(
            AuditAction action,
            Pageable pageable
    ) {
        if (action == null) {
            throw new InvalidAuditQueryException(
                    "Audit action is required"
            );
        }

        validatePageable(pageable);

        return auditLogRepository
                .findByActionOrderByCreatedAtDesc(
                        action,
                        pageable
                )
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByEntity(
            AuditEntityType entityType,
            String entityId,
            Pageable pageable
    ) {
        if (entityType == null) {
            throw new InvalidAuditQueryException(
                    "Entity type is required"
            );
        }

        String normalizedEntityId =
                normalizeRequiredText(
                        entityId,
                        "Entity ID"
                );

        validatePageable(pageable);

        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        entityType,
                        normalizedEntityId,
                        pageable
                )
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse>
    getAuditLogsByServiceName(
            String serviceName,
            Pageable pageable
    ) {
        String normalizedServiceName =
                normalizeRequiredText(
                        serviceName,
                        "Service name"
                )
                        .toLowerCase(Locale.ROOT);

        validatePageable(pageable);

        return auditLogRepository
                .findByServiceNameOrderByCreatedAtDesc(
                        normalizedServiceName,
                        pageable
                )
                .map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByRequestId(
            String requestId,
            Pageable pageable
    ) {
        String normalizedRequestId =
                normalizeRequiredText(
                        requestId,
                        "Request ID"
                );

        validatePageable(pageable);

        return auditLogRepository
                .findByRequestIdOrderByCreatedAtDesc(
                        normalizedRequestId,
                        pageable
                )
                .map(auditLogMapper::toResponse);
    }

    private AuditLog findAuditLogById(
            Long auditLogId
    ) {
        return auditLogRepository
                .findById(auditLogId)
                .orElseThrow(() ->
                        new AuditLogNotFoundException(
                                "Audit log not found with ID: "
                                        + auditLogId
                        )
                );
    }

    private void validatePositiveId(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0) {
            throw new InvalidAuditQueryException(
                    fieldName + " must be positive"
            );
        }
    }

    private void validatePageable(
            Pageable pageable
    ) {
        if (pageable == null || pageable.isUnpaged()) {
            throw new InvalidAuditQueryException(
                    "Pagination information is required"
            );
        }

        if (pageable.getPageNumber() < 0) {
            throw new InvalidAuditQueryException(
                    "Page number cannot be negative"
            );
        }

        if (pageable.getPageSize() <= 0) {
            throw new InvalidAuditQueryException(
                    "Page size must be greater than zero"
            );
        }

        if (pageable.getPageSize()
                > MAXIMUM_PAGE_SIZE) {

            throw new InvalidAuditQueryException(
                    "Page size cannot exceed "
                            + MAXIMUM_PAGE_SIZE
            );
        }
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new InvalidAuditQueryException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }
}