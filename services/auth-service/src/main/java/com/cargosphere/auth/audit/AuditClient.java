package com.cargosphere.auth.audit;

public interface AuditClient {

    void publish(AuditEventRequest request);
}