package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.dto.CargoDetailRequest;
import com.cargosphere.shipment.dto.CargoDetailResponse;
import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.ShipmentEventResponse;
import com.cargosphere.shipment.dto.ShipmentResponse;
import com.cargosphere.shipment.dto.UpdateShipmentStatusRequest;
import com.cargosphere.shipment.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("""
            hasRole('ADMIN') or
            (
                hasRole('CLIENT') and
                @shipmentAuthorizationService.isCurrentUser(
                    #p0.clientUserId,
                    authentication
                )
            )
            """)
    public ShipmentResponse createShipment(
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        return shipmentService.createShipment(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ShipmentResponse> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    @GetMapping("/{shipmentId}")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public ShipmentResponse getShipmentById(
            @PathVariable Long shipmentId
    ) {
        return shipmentService.getShipmentById(shipmentId);
    }

    @GetMapping("/number/{shipmentNumber}")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentNumberOwner(
                #p0,
                authentication
            )
            """)
    public ShipmentResponse getShipmentByNumber(
            @PathVariable String shipmentNumber
    ) {
        return shipmentService.getShipmentByNumber(
                shipmentNumber
        );
    }

    @GetMapping("/client/{clientUserId}")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isCurrentUser(
                #p0,
                authentication
            )
            """)
    public List<ShipmentResponse> getShipmentsByClientUserId(
            @PathVariable Long clientUserId
    ) {
        return shipmentService
                .getShipmentsByClientUserId(clientUserId);
    }

    @PostMapping("/{shipmentId}/cargo-details")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public CargoDetailResponse addCargoDetail(
            @PathVariable Long shipmentId,
            @Valid @RequestBody CargoDetailRequest request
    ) {
        return shipmentService.addCargoDetail(
                shipmentId,
                request
        );
    }

    @GetMapping("/{shipmentId}/cargo-details")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public List<CargoDetailResponse> getCargoDetailsByShipmentId(
            @PathVariable Long shipmentId
    ) {
        return shipmentService
                .getCargoDetailsByShipmentId(shipmentId);
    }

    @PatchMapping("/{shipmentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ShipmentResponse updateShipmentStatus(
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        return shipmentService.updateShipmentStatus(
                shipmentId,
                request
        );
    }

    @GetMapping("/{shipmentId}/events")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public List<ShipmentEventResponse> getShipmentEventsByShipmentId(
            @PathVariable Long shipmentId
    ) {
        return shipmentService
                .getShipmentEventsByShipmentId(shipmentId);
    }
}