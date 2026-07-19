package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.CargoDetailRequest;
import com.cargosphere.shipment.dto.CargoDetailResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.Shipment;
import org.springframework.stereotype.Component;

@Component
public class CargoDetailMapper {

    public CargoDetail toEntity(CargoDetailRequest request, Shipment shipment) {
        if (request == null) {
            return null;
        }

        return CargoDetail.builder()
                .shipment(shipment)
                .cargoName(request.getCargoName())
                .cargoDescription(request.getCargoDescription())
                .cargoType(request.getCargoType())
                .weightKg(request.getWeightKg())
                .volumeCbm(request.getVolumeCbm())
                .quantity(request.getQuantity())
                .fragile(request.getFragile())
                .hazardous(request.getHazardous())
                .build();
    }

    public CargoDetailResponse toResponse(CargoDetail cargoDetail) {
        if (cargoDetail == null) {
            return null;
        }

        return CargoDetailResponse.builder()
                .id(cargoDetail.getId())
                .shipmentId(cargoDetail.getShipment().getId())
                .cargoName(cargoDetail.getCargoName())
                .cargoDescription(cargoDetail.getCargoDescription())
                .cargoType(cargoDetail.getCargoType())
                .weightKg(cargoDetail.getWeightKg())
                .volumeCbm(cargoDetail.getVolumeCbm())
                .quantity(cargoDetail.getQuantity())
                .fragile(cargoDetail.getFragile())
                .hazardous(cargoDetail.getHazardous())
                .createdAt(cargoDetail.getCreatedAt())
                .updatedAt(cargoDetail.getUpdatedAt())
                .build();
    }
}