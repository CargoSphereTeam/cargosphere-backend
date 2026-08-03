package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.admin.CargoVerificationItemRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationItemResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import org.springframework.stereotype.Component;

@Component
public class CargoVerificationMapper {

    public CargoVerification toNewEntity(
            CargoDetail cargoDetail,
            CargoVerificationItemRequest request
    ) {
        if (cargoDetail == null || request == null) {
            return null;
        }

        CargoVerification cargoVerification =
                CargoVerification.builder()
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.DRAFT
                        )
                        .build();

        updateEntity(cargoVerification, request);

        return cargoVerification;
    }

    public void updateEntity(
            CargoVerification cargoVerification,
            CargoVerificationItemRequest request
    ) {
        if (cargoVerification == null || request == null) {
            return;
        }

        cargoVerification.setConfirmedCargoName(
                request.getConfirmedCargoName()
        );

        cargoVerification.setConfirmedCargoDescription(
                request.getConfirmedCargoDescription()
        );

        cargoVerification.setConfirmedCargoType(
                request.getConfirmedCargoType()
        );

        cargoVerification.setConfirmedWeightKg(
                request.getConfirmedWeightKg()
        );

        cargoVerification.setConfirmedVolumeCbm(
                request.getConfirmedVolumeCbm()
        );

        cargoVerification.setConfirmedQuantity(
                request.getConfirmedQuantity()
        );

        cargoVerification.setConfirmedFragile(
                request.getConfirmedFragile()
        );

        cargoVerification.setConfirmedHazardous(
                request.getConfirmedHazardous()
        );

        cargoVerification.setVerificationRemarks(
                request.getVerificationRemarks()
        );
    }

    public CargoVerificationItemResponse toResponse(
            CargoVerification cargoVerification
    ) {
        if (cargoVerification == null) {
            return null;
        }

        return CargoVerificationItemResponse.builder()
                .id(cargoVerification.getId())
                .cargoDetailId(
                        cargoVerification
                                .getCargoDetail()
                                .getId()
                )
                .verificationStatus(
                        cargoVerification.getVerificationStatus()
                )
                .confirmedCargoName(
                        cargoVerification.getConfirmedCargoName()
                )
                .confirmedCargoDescription(
                        cargoVerification
                                .getConfirmedCargoDescription()
                )
                .confirmedCargoType(
                        cargoVerification.getConfirmedCargoType()
                )
                .confirmedWeightKg(
                        cargoVerification.getConfirmedWeightKg()
                )
                .confirmedVolumeCbm(
                        cargoVerification.getConfirmedVolumeCbm()
                )
                .confirmedQuantity(
                        cargoVerification.getConfirmedQuantity()
                )
                .confirmedFragile(
                        cargoVerification.getConfirmedFragile()
                )
                .confirmedHazardous(
                        cargoVerification.getConfirmedHazardous()
                )
                .verificationRemarks(
                        cargoVerification.getVerificationRemarks()
                )
                .verifiedBy(cargoVerification.getVerifiedBy())
                .verifiedAt(cargoVerification.getVerifiedAt())
                .createdAt(cargoVerification.getCreatedAt())
                .updatedAt(cargoVerification.getUpdatedAt())
                .build();
    }
}