package com.cargosphere.container.controller;

import com.cargosphere.container.dto.AllocationRequest;
import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.service.ShipmentContainerAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/container-allocations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShipmentContainerAllocationController {

    private final ShipmentContainerAllocationService allocationService;

    @PostMapping
    public ResponseEntity<AllocationResponse> createAllocation(
            @Valid @RequestBody AllocationRequest request
    ) {
        AllocationResponse response =
                allocationService.createAllocation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AllocationResponse>> getAllAllocations() {
        return ResponseEntity.ok(
                allocationService.getAllAllocations()
        );
    }

    @GetMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> getAllocationById(
            @PathVariable Long allocationId
    ) {
        return ResponseEntity.ok(
                allocationService.getAllocationById(allocationId)
        );
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<AllocationResponse>>
    getAllocationsByShipmentId(
            @PathVariable Long shipmentId
    ) {
        return ResponseEntity.ok(
                allocationService.getAllocationsByShipmentId(shipmentId)
        );
    }

    @PutMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> updateAllocation(
            @PathVariable Long allocationId,
            @Valid @RequestBody AllocationRequest request
    ) {
        return ResponseEntity.ok(
                allocationService.updateAllocation(
                        allocationId,
                        request
                )
        );
    }

    @DeleteMapping("/{allocationId}")
    public ResponseEntity<Void> deleteAllocation(
            @PathVariable Long allocationId
    ) {
        allocationService.deleteAllocation(allocationId);

        return ResponseEntity.noContent().build();
    }
}