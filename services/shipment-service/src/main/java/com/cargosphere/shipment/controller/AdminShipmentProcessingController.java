package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.dto.admin.CargoVerificationRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationResponse;
import com.cargosphere.shipment.dto.admin.ProcessingContinueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingQueueResponse;
import com.cargosphere.shipment.dto.admin.ProcessingReadinessResponse;
import com.cargosphere.shipment.dto.admin.ProcessingStartResponse;
import com.cargosphere.shipment.dto.ebill.EbillGenerationResponse;
import com.cargosphere.shipment.dto.ebill.EbillPreviewResponse;
import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.service.AdminShipmentProcessingService;
import com.cargosphere.shipment.service.CargoVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/processing/queue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get administrator shipment-processing queue",
            description =
                    "Returns a paginated shipment-processing queue. "
                            + "The optional stage parameter filters shipments "
                            + "by their current processing stage."
    )
    public ResponseEntity<ProcessingQueueResponse>
    getProcessingQueue(
            @RequestParam(
                    name = "stage",
                    required = false
            )
            ProcessingStage processingStage,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            @Min(
                    value = 0,
                    message = "Page number must be zero or greater"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "20"
            )
            @Min(
                    value = 1,
                    message = "Page size must be at least 1"
            )
            @Max(
                    value = 100,
                    message = "Page size must not exceed 100"
            )
            int size
    ) {
        ProcessingQueueResponse response =
                adminShipmentProcessingService
                        .getProcessingQueue(
                                processingStage,
                                page,
                                size
                        );

        return ResponseEntity.ok(response);
    }

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

    @PostMapping("/{shipmentId}/processing/continue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Continue shipment processing",
            description =
                    "Validates the current processing stage requirements "
                            + "and advances the shipment to the next stage"
    )
    public ResponseEntity<ProcessingContinueResponse>
    continueProcessing(
            @PathVariable
            @Positive(message = "Shipment ID must be greater than zero")
            Long shipmentId
    ) {
        ProcessingContinueResponse response =
                adminShipmentProcessingService
                        .continueProcessing(shipmentId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{shipmentId}/processing/readiness")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get shipment processing readiness",
            description =
                    "Calculates container, cargo, document, payment "
                            + "and eBill readiness for a shipment"
    )
    public ResponseEntity<ProcessingReadinessResponse>
    getProcessingReadiness(
            @PathVariable
            @Positive(message = "Shipment ID must be greater than zero")
            Long shipmentId
    ) {
        ProcessingReadinessResponse response =
                adminShipmentProcessingService
                        .getProcessingReadiness(shipmentId);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{shipmentId}/ebill")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Generate shipment eBill",
            description =
                    "Validates all shipment-processing requirements, "
                            + "creates an immutable eBill snapshot and "
                            + "stores the generated eBill metadata"
    )
    public ResponseEntity<EbillGenerationResponse>
    generateEbill(
            @PathVariable
            @Positive(message = "Shipment ID must be greater than zero")
            Long shipmentId
    ) {
        EbillGenerationResponse response =
                adminShipmentProcessingService
                        .generateEbill(shipmentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{shipmentId}/ebill-preview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Preview shipment eBill",
            description =
                    "Returns live shipment, client, cargo, container, "
                            + "document, payment, event and readiness data "
                            + "before immutable eBill generation"
    )
    public ResponseEntity<EbillPreviewResponse>
    getEbillPreview(
            @PathVariable
            @Positive(message = "Shipment ID must be greater than zero")
            Long shipmentId
    ) {
        EbillPreviewResponse response =
                adminShipmentProcessingService
                        .getEbillPreview(shipmentId);

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
