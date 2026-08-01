package com.cargosphere.container.controller;

import com.cargosphere.container.config.OpenApiConfig;
import com.cargosphere.container.dto.ContainerTypeRequest;
import com.cargosphere.container.dto.ContainerTypeResponse;
import com.cargosphere.container.exception.ErrorResponse;
import com.cargosphere.container.service.ContainerTypeService;
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
@RequestMapping("/api/container-types")
@RequiredArgsConstructor
@Tag(
        name = "Container Types",
        description = "Container type catalogue management"
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class ContainerTypeController {

    private final ContainerTypeService containerTypeService;

    @Operation(
            summary = "Create a container type",
            description = "Creates a new container type. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Container type created successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ContainerTypeResponse.class
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
                    responseCode = "409",
                    description = "Container type code already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContainerTypeResponse> createContainerType(
            @Valid @RequestBody ContainerTypeRequest request
    ) {
        ContainerTypeResponse response =
                containerTypeService.createContainerType(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all container types",
            description =
                    "Returns all container types. Accessible to ADMIN and CLIENT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Container types returned successfully",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation =
                                                    ContainerTypeResponse.class
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
                    description = "ADMIN or CLIENT role is required"
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<ContainerTypeResponse>>
    getAllContainerTypes() {
        return ResponseEntity.ok(
                containerTypeService.getAllContainerTypes()
        );
    }

    @Operation(
            summary = "Get container type by ID",
            description =
                    "Returns one container type. Accessible to ADMIN and CLIENT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Container type returned successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ContainerTypeResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN or CLIENT role is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Container type was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping("/{containerTypeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ContainerTypeResponse> getContainerTypeById(
            @Parameter(
                    description = "Container type database ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long containerTypeId
    ) {
        return ResponseEntity.ok(
                containerTypeService.getContainerTypeById(containerTypeId)
        );
    }

    @Operation(
            summary = "Update a container type",
            description =
                    "Updates an existing container type. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Container type updated successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ContainerTypeResponse.class
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
                    description = "Container type was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Container type code already exists",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping("/{containerTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContainerTypeResponse> updateContainerType(
            @Parameter(
                    description = "Container type database ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long containerTypeId,

            @Valid
            @RequestBody ContainerTypeRequest request
    ) {
        return ResponseEntity.ok(
                containerTypeService.updateContainerType(
                        containerTypeId,
                        request
                )
        );
    }

    @Operation(
            summary = "Delete a container type",
            description =
                    "Deletes an existing container type. Requires ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Container type deleted successfully"
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
                    description = "Container type was not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @DeleteMapping("/{containerTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContainerType(
            @Parameter(
                    description = "Container type database ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long containerTypeId
    ) {
        containerTypeService.deleteContainerType(containerTypeId);

        return ResponseEntity.noContent().build();
    }
}
