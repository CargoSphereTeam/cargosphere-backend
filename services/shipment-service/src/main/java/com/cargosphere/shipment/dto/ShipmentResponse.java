package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {

    private Long id;

    private String shipmentNumber;

    private Long clientUserId;

    private String originLocation;

    private String destinationLocation;

    private ShipmentType shipmentType;

    private ShipmentStatus status;

    private LocalDate expectedPickupDate;

    private LocalDate expectedDeliveryDate;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}