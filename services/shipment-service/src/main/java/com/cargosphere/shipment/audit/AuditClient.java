package com.cargosphere.shipment.audit;

public interface AuditClient {

    void publish(AuditEventRequest request);
}