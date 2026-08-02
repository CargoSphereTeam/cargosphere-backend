package com.cargosphere.shipment.service;

import com.cargosphere.shipment.dto.admin.ProcessingContinueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.dto.ebill.EbillPreviewResponse;
import com.cargosphere.shipment.entity.enums.ProcessingStage;

public interface AdminShipmentProcessingService {

    ProcessingStartResponse startProcessing(Long shipmentId);

    ProcessingContinueResponse continueProcessing(
            Long shipmentId
    );

    ProcessingQueueResponse getProcessingQueue(
            ProcessingStage processingStage,
            int page,
            int size
    );

    ProcessingReadinessResponse getProcessingReadiness(
            Long shipmentId
    );

    EbillPreviewResponse getEbillPreview(
            Long shipmentId
    );
}
