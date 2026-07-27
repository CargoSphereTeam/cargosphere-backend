package com.cargosphere.container.audit;

public interface AuditClient {

    void send(AuditEventRequest request);
}