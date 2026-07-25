package com.cargosphere.audit.repository;

import com.cargosphere.audit.entity.AuditLog;
import com.cargosphere.audit.entity.enums.AuditAction;
import com.cargosphere.audit.entity.enums.AuditEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    Page<AuditLog>
    findByActorUserIdOrderByCreatedAtDesc(
            Long actorUserId,
            Pageable pageable
    );

    Page<AuditLog>
    findByActionOrderByCreatedAtDesc(
            AuditAction action,
            Pageable pageable
    );

    Page<AuditLog>
    findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            AuditEntityType entityType,
            String entityId,
            Pageable pageable
    );

    Page<AuditLog>
    findByServiceNameOrderByCreatedAtDesc(
            String serviceName,
            Pageable pageable
    );

    Page<AuditLog>
    findByRequestIdOrderByCreatedAtDesc(
            String requestId,
            Pageable pageable
    );
}