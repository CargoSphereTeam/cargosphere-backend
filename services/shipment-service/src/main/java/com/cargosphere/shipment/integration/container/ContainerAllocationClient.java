package com.cargosphere.shipment.integration.container;

import java.util.List;

public interface ContainerAllocationClient {

    List<ContainerAllocationResponse>
    getAllocationsByShipmentId(Long shipmentId);
}
