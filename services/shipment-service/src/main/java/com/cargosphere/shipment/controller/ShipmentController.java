package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.config.OpenApiConfig;
import com.cargosphere.shipment.dto.CargoDetailRequest;
import com.cargosphere.shipment.dto.CargoDetailResponse;
import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.ShipmentEventResponse;
import com.cargosphere.shipment.dto.ShipmentResponse;
import com.cargosphere.shipment.dto.UpdateShipmentStatusRequest;
import com.cargosphere.shipment.exception.ApiErrorResponse;
import com.cargosphere.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Shipments",
        description =
                "Shipment creation, cargo management, status updates "
                        + "and shipment event history"
)
@SecurityRequirement(
        name = OpenApiConfig.SECURITY_SCHEME_NAME
)
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Operation(
            summary = "Create a shipment",
            description =
                    "Creates a new shipment. ADMIN can create a shipment "
                            + "for any client. CLIENT can create a shipment "
                            + "only for their own user ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Shipment created successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ShipmentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "CLIENT attempted to create a shipment "
                                    + "for another user"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
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

    @Operation(
            summary = "Get all shipments",
            description =
                    "Returns every shipment available in the system. "
                            + "This endpoint requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipments returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    ShipmentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ShipmentResponse> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    @Operation(
            summary = "Get shipment by ID",
            description =
                    "ADMIN can retrieve any shipment. CLIENT can retrieve "
                            + "only a shipment owned by their account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ShipmentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "The authenticated client does not own "
                                    + "the requested shipment"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{shipmentId}")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public ShipmentResponse getShipmentById(
            @Parameter(
                    description = "Shipment database ID",
                    example = "101",
                    required = true
            )
            @PathVariable Long shipmentId
    ) {
        return shipmentService.getShipmentById(shipmentId);
    }

    @Operation(
            summary = "Get shipment by shipment number",
            description =
                    "Returns a shipment using its generated shipment number. "
                            + "ADMIN can retrieve any shipment. CLIENT can "
                            + "retrieve only a shipment owned by their account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ShipmentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "The authenticated client does not own "
                                    + "the requested shipment"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/number/{shipmentNumber}")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentNumberOwner(
                #p0,
                authentication
            )
            """)
    public ShipmentResponse getShipmentByNumber(
            @Parameter(
                    description = "Generated shipment number",
                    example = "CS-2026-000001",
                    required = true
            )
            @PathVariable String shipmentNumber
    ) {
        return shipmentService.getShipmentByNumber(
                shipmentNumber
        );
    }

    @Operation(
            summary = "Get shipments by client user ID",
            description =
                    "Returns all shipments belonging to a client. ADMIN can "
                            + "request shipments for any client. CLIENT can "
                            + "request only their own shipments."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Client shipments returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    ShipmentResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "CLIENT attempted to access another "
                                    + "user's shipments"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/client/{clientUserId}")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isCurrentUser(
                #p0,
                authentication
            )
            """)
    public List<ShipmentResponse> getShipmentsByClientUserId(
            @Parameter(
                    description = "Client user ID",
                    example = "25",
                    required = true
            )
            @PathVariable Long clientUserId
    ) {
        return shipmentService
                .getShipmentsByClientUserId(clientUserId);
    }

    @Operation(
            summary = "Add cargo details",
            description =
                    "Adds a cargo item to a shipment. ADMIN can update "
                            + "any shipment. CLIENT must own the shipment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cargo detail added successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            CargoDetailResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "The authenticated client does not own "
                                    + "the shipment"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
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
            @Parameter(
                    description = "Shipment database ID",
                    example = "101",
                    required = true
            )
            @PathVariable Long shipmentId,

            @Valid
            @RequestBody CargoDetailRequest request
    ) {
        return shipmentService.addCargoDetail(
                shipmentId,
                request
        );
    }

    @Operation(
            summary = "Get shipment cargo details",
            description =
                    "Returns all cargo items associated with a shipment. "
                            + "ADMIN can access any shipment. CLIENT must "
                            + "own the shipment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cargo details returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    CargoDetailResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "The authenticated client does not own "
                                    + "the shipment"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{shipmentId}/cargo-details")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public List<CargoDetailResponse> getCargoDetailsByShipmentId(
            @Parameter(
                    description = "Shipment database ID",
                    example = "101",
                    required = true
            )
            @PathVariable Long shipmentId
    ) {
        return shipmentService
                .getCargoDetailsByShipmentId(shipmentId);
    }

    @Operation(
            summary = "Update shipment status",
            description =
                    "Updates the shipment lifecycle status. "
                            + "This endpoint requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment status updated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ShipmentResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description =
                            "Request validation failed or the status "
                                    + "transition is not allowed",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @PatchMapping("/{shipmentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ShipmentResponse updateShipmentStatus(
            @Parameter(
                    description = "Shipment database ID",
                    example = "101",
                    required = true
            )
            @PathVariable Long shipmentId,

            @Valid
            @RequestBody UpdateShipmentStatusRequest request
    ) {
        return shipmentService.updateShipmentStatus(
                shipmentId,
                request
        );
    }

    @Operation(
            summary = "Get shipment event history",
            description =
                    "Returns the ordered event history for a shipment. "
                            + "ADMIN can access any shipment. CLIENT must "
                            + "own the shipment."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment events returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    ShipmentEventResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description =
                            "The authenticated client does not own "
                                    + "the shipment"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipment was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{shipmentId}/events")
    @PreAuthorize("""
            hasRole('ADMIN') or
            @shipmentAuthorizationService.isShipmentOwner(
                #p0,
                authentication
            )
            """)
    public List<ShipmentEventResponse> getShipmentEventsByShipmentId(
            @Parameter(
                    description = "Shipment database ID",
                    example = "101",
                    required = true
            )
            @PathVariable Long shipmentId
    ) {
        return shipmentService
                .getShipmentEventsByShipmentId(shipmentId);
    }
}
