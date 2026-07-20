package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.dto.*;
import com.cargosphere.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        return shipmentService.createShipment(request);
    }

    @GetMapping
    public List<ShipmentResponse> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    @GetMapping("/{shipmentId}")
    public ShipmentResponse getShipmentById(@PathVariable Long shipmentId) {
        return shipmentService.getShipmentById(shipmentId);
    }

    @GetMapping("/number/{shipmentNumber}")
    public ShipmentResponse getShipmentByNumber(@PathVariable String shipmentNumber) {
        return shipmentService.getShipmentByNumber(shipmentNumber);
    }

    @GetMapping("/client/{clientUserId}")
    public List<ShipmentResponse> getShipmentsByClientUserId(@PathVariable Long clientUserId) {
        return shipmentService.getShipmentsByClientUserId(clientUserId);
    }

    @PostMapping("/{shipmentId}/cargo-details")
    @ResponseStatus(HttpStatus.CREATED)
    public CargoDetailResponse addCargoDetail(
            @PathVariable Long shipmentId,
            @Valid @RequestBody CargoDetailRequest request
    ) {
        return shipmentService.addCargoDetail(shipmentId, request);
    }

    @GetMapping("/{shipmentId}/cargo-details")
    public List<CargoDetailResponse> getCargoDetailsByShipmentId(@PathVariable Long shipmentId) {
        return shipmentService.getCargoDetailsByShipmentId(shipmentId);
    }

    @PatchMapping("/{shipmentId}/status")
    public ShipmentResponse updateShipmentStatus(
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        return shipmentService.updateShipmentStatus(shipmentId, request);
    }

    @GetMapping("/{shipmentId}/events")
    public List<ShipmentEventResponse> getShipmentEventsByShipmentId(@PathVariable Long shipmentId) {
        return shipmentService.getShipmentEventsByShipmentId(shipmentId);
    }
}