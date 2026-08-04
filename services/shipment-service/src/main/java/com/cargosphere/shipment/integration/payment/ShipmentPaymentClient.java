package com.cargosphere.shipment.integration.payment;

import java.util.List;

public interface ShipmentPaymentClient {

    List<ShipmentPaymentResponse>
    getPaymentsByShipmentId(Long shipmentId);

    ShipmentPaymentSummaryResponse
    getPaymentSummary(Long shipmentId);
}