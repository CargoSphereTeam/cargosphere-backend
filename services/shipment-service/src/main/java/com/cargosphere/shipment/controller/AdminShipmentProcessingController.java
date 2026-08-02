package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.service.AdminShipmentProcessingService;
import com.cargosphere.shipment.service.CargoVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Admin Shipment Processing",
        description = "Administrative shipment-processing workflow APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class AdminShipmentProcessingController {

    private final CargoVerificationService
            cargoVerificationService;

    private final AdminShipmentProcessingService
            adminShipmentProcessingService;

    @PostMapping("/{shipmentId}/processing/start")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Start administrative shipment processing",
            description =
                    "Moves a shipment from pending administrator review "
                            + "to container allocation"
    )
    public ResponseEntity<ProcessingStartResponse>
    startProcessing(
            @PathVariable
            @Positive(message = "Shipment ID must be greater than zero")
            Long shipmentId
    ) {
        ProcessingStartResponse response =
                adminShipmentProcessingService.startProcessing(
                        shipmentId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shipmentId}/cargo-verification")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Save or confirm cargo verification",
            description =
                    "Saves cargo verification as a draft or confirms "
                            + "all cargo items and advances the shipment "
                            + "to document verification"
    )
    public ResponseEntity<CargoVerificationResponse>
    saveOrConfirmCargoVerification(
            @PathVariable
            @Positive(message = "Shipment ID must be greater than zero")
            Long shipmentId,

            @Valid
            @RequestBody
            CargoVerificationRequest request
    ) {
        CargoVerificationResponse response =
                cargoVerificationService.saveOrConfirm(
                        shipmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}