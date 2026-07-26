package com.cargosphere.documentservice.audit;

public record AuditEventRequest(

        Long actorUserId,

        String actorRole,

        String action,

        String entityType,

        String entityId,

        String serviceName,

        String description,

        String outcome,

        String requestId,

        String ipAddress,

        String httpMethod,

        String endpoint,

        Integer statusCode
) {
}