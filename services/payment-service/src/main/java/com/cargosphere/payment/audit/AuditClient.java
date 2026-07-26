package com.cargosphere.payment.audit;

public interface AuditClient {

    void publish(AuditEventRequest request);
}