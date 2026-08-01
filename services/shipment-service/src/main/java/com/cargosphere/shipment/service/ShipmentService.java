package com.cargosphere.shipment.service;

import com.cargosphere.shipment.dto.*;

import java.util.List;

public interface ShipmentService {

    ShipmentResponse createShipment(CreateShipmentRequest request);

    ShipmentResponse getShipmentById(Long shipmentId);

    ShipmentResponse getShipmentByNumber(String shipmentNumber);

    List<ShipmentResponse> getAllShipments();

    List<ShipmentResponse> getShipmentsByClientUserId(Long clientUserId);

    CargoDetailResponse addCargoDetail(Long shipmentId, CargoDetailRequest request);

    List<CargoDetailResponse> getCargoDetailsByShipmentId(Long shipmentId);

    ShipmentResponse updateShipmentStatus(Long shipmentId, UpdateShipmentStatusRequest request);

    List<ShipmentEventResponse> getShipmentEventsByShipmentId(Long shipmentId);
}