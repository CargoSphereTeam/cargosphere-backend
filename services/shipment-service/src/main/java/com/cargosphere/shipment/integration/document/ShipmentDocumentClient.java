package com.cargosphere.shipment.integration.document;

import java.util.List;

public interface ShipmentDocumentClient {

    List<ShipmentDocumentResponse>
    getDocumentsByShipmentId(Long shipmentId);
}
