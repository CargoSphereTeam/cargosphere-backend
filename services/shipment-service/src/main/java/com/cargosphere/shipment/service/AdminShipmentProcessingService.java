package com.cargosphere.shipment.service;

import com.cargosphere.shipment.dto.admin.ProcessingQueueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.entity.enums.ProcessingStage;

public interface AdminShipmentProcessingService {

    ProcessingStartResponse startProcessing(Long shipmentId);

    ProcessingQueueResponse getProcessingQueue(
            ProcessingStage processingStage,
            int page,
            int size
    );
}