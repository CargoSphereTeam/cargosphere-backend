package com.cargosphere.shipment.service;

import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;

public interface CargoVerificationService {

    CargoVerificationResponse saveOrConfirm(
            Long shipmentId,
            CargoVerificationRequest request
    );
}