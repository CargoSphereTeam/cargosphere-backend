package com.cargosphere.container.controller;

import com.cargosphere.container.config.OpenApiConfig;
import com.cargosphere.container.dto.AllocationRequest;
import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.exception.ErrorResponse;
import com.cargosphere.container.service.ShipmentContainerAllocationService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/container-allocations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Container Allocations",
        description =
                "ADMIN shipment-container allocation management"
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class ShipmentContainerAllocationController {

    private final ShipmentContainerAllocationService allocationService;

    @Operation(
            summary = "Create a container allocation",
            description =
                    "Allocates a container type to a shipment. "
                            + "Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Allocation created successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AllocationResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
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
                    description =
                            "Shipment or container type was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<AllocationResponse> createAllocation(
            @Valid @RequestBody AllocationRequest request
    ) {
        AllocationResponse response =
                allocationService.createAllocation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all allocations",
            description =
                    "Returns all container allocations. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Allocations returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    AllocationResponse.class
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
            )
    })
    @GetMapping
    public ResponseEntity<List<AllocationResponse>> getAllAllocations() {
        return ResponseEntity.ok(
                allocationService.getAllAllocations()
        );
    }

    @Operation(
            summary = "Get allocation by ID",
            description =
                    "Returns one container allocation. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Allocation returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AllocationResponse.class
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
                    description = "Allocation was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> getAllocationById(
            @Parameter(
                    description = "Allocation database ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long allocationId
    ) {
        return ResponseEntity.ok(
                allocationService.getAllocationById(allocationId)
        );
    }

    @Operation(
            summary = "Get allocations by shipment ID",
            description =
                    "Returns allocations assigned to one shipment. "
                            + "Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipment allocations returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    AllocationResponse.class
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
            )
    })
    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<List<AllocationResponse>>
    getAllocationsByShipmentId(
            @Parameter(
                    description = "Shipment database ID",
                    example = "101",
                    required = true
            )
            @PathVariable Long shipmentId
    ) {
        return ResponseEntity.ok(
                allocationService.getAllocationsByShipmentId(shipmentId)
        );
    }

    @Operation(
            summary = "Update a container allocation",
            description =
                    "Updates an existing allocation. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Allocation updated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AllocationResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
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
                    description =
                            "Allocation, shipment or container type "
                                    + "was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> updateAllocation(
            @Parameter(
                    description = "Allocation database ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long allocationId,

            @Valid
            @RequestBody AllocationRequest request
    ) {
        return ResponseEntity.ok(
                allocationService.updateAllocation(
                        allocationId,
                        request
                )
        );
    }

    @Operation(
            summary = "Delete a container allocation",
            description =
                    "Deletes an existing allocation. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Allocation deleted successfully"
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
                    description = "Allocation was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{allocationId}")
    public ResponseEntity<Void> deleteAllocation(
            @Parameter(
                    description = "Allocation database ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long allocationId
    ) {
        allocationService.deleteAllocation(allocationId);

        return ResponseEntity.noContent().build();
    }
}
