package com.cargosphere.documentservice.audit;

public interface AuditClient {

    void publish(AuditEventRequest request);
}