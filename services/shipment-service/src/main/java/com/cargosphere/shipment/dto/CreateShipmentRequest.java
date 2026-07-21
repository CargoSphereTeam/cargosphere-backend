package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.ShipmentType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateShipmentRequest {

    @NotNull(message = "Client user id is required")
    @Positive(message = "Client user id must be greater than zero")
    private Long clientUserId;

    @NotBlank(message = "Origin location is required")
    @Size(
            max = 150,
            message = "Origin location must not exceed 150 characters"
    )
    private String originLocation;

    @NotBlank(message = "Destination location is required")
    @Size(
            max = 150,
            message = "Destination location must not exceed 150 characters"
    )
    private String destinationLocation;

    @NotNull(message = "Shipment type is required")
    private ShipmentType shipmentType;

    @FutureOrPresent(
            message = "Expected pickup date cannot be in the past"
    )
    private LocalDate expectedPickupDate;

    @FutureOrPresent(
            message = "Expected delivery date cannot be in the past"
    )
    private LocalDate expectedDeliveryDate;
}