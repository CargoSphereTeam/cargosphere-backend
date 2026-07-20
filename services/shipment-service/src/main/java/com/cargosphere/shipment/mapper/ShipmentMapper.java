package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.ShipmentResponse;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public Shipment toEntity(CreateShipmentRequest request, String shipmentNumber) {
        if (request == null) {
            return null;
        }

        return Shipment.builder()
                .shipmentNumber(shipmentNumber)
                .clientUserId(request.getClientUserId())
                .originLocation(request.getOriginLocation())
                .destinationLocation(request.getDestinationLocation())
                .shipmentType(request.getShipmentType())
                .status(ShipmentStatus.CREATED)
                .expectedPickupDate(request.getExpectedPickupDate())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .build();
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        if (shipment == null) {
            return null;
        }

        return ShipmentResponse.builder()
                .id(shipment.getId())
                .shipmentNumber(shipment.getShipmentNumber())
                .clientUserId(shipment.getClientUserId())
                .originLocation(shipment.getOriginLocation())
                .destinationLocation(shipment.getDestinationLocation())
                .shipmentType(shipment.getShipmentType())
                .status(shipment.getStatus())
                .expectedPickupDate(shipment.getExpectedPickupDate())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}