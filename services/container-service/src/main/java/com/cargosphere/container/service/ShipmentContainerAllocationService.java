package com.cargosphere.container.service;

import com.cargosphere.container.dto.AllocationRequest;
import com.cargosphere.container.dto.AllocationResponse;

import java.util.List;

public interface ShipmentContainerAllocationService {

    AllocationResponse createAllocation(AllocationRequest request);

    List<AllocationResponse> getAllAllocations();

    AllocationResponse getAllocationById(Long allocationId);

    List<AllocationResponse> getAllocationsByShipmentId(Long shipmentId);

    AllocationResponse updateAllocation(
            Long allocationId,
            AllocationRequest request
    );

    void deleteAllocation(Long allocationId);
}