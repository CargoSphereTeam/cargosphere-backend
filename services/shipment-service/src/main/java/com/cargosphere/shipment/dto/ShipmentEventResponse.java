package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEventResponse {

    private Long id;

    private Long shipmentId;

    private ShipmentEventType eventType;

    private String eventDescription;

    private String eventLocation;

    private LocalDateTime eventTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}