package com.cargosphere.shipment.service;

import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;

public interface AdminShipmentProcessingService {

    ProcessingStartResponse startProcessing(Long shipmentId);
}