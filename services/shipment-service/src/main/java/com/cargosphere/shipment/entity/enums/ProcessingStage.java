package com.cargosphere.shipment.entity.enums;

public enum ProcessingStage {
    PENDING_ADMIN_REVIEW,
    CONTAINER_ALLOCATION,
    CARGO_VERIFICATION,
    DOCUMENT_VERIFICATION,
    PAYMENT_CONFIRMATION,
    READY_FOR_EBILL,
    EBILL_GENERATED
}