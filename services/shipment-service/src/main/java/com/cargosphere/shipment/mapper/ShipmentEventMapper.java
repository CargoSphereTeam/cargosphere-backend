package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.ShipmentEventResponse;
import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventMapper {

    public ShipmentEvent toEntity(
            Shipment shipment,
            ShipmentEventType eventType,
            String eventDescription,
            String eventLocation
    ) {
        return ShipmentEvent.builder()
                .shipment(shipment)
                .eventType(eventType)
                .eventDescription(eventDescription)
                .eventLocation(eventLocation)
                .build();
    }

    public ShipmentEventResponse toResponse(ShipmentEvent shipmentEvent) {
        if (shipmentEvent == null) {
            return null;
        }

        return ShipmentEventResponse.builder()
                .id(shipmentEvent.getId())
                .shipmentId(shipmentEvent.getShipment().getId())
                .eventType(shipmentEvent.getEventType())
                .eventDescription(shipmentEvent.getEventDescription())
                .eventLocation(shipmentEvent.getEventLocation())
                .eventTime(shipmentEvent.getEventTime())
                .createdAt(shipmentEvent.getCreatedAt())
                .updatedAt(shipmentEvent.getUpdatedAt())
                .build();
    }
}