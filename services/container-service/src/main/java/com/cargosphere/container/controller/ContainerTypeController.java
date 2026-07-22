package com.cargosphere.container.controller;

import com.cargosphere.container.dto.ContainerTypeRequest;
import com.cargosphere.container.dto.ContainerTypeResponse;
import com.cargosphere.container.service.ContainerTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/container-types")
@RequiredArgsConstructor
public class ContainerTypeController {

    private final ContainerTypeService containerTypeService;

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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<ContainerTypeResponse>> getAllContainerTypes() {
        return ResponseEntity.ok(
                containerTypeService.getAllContainerTypes()
        );
    }

    @GetMapping("/{containerTypeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ContainerTypeResponse> getContainerTypeById(
            @PathVariable Long containerTypeId
    ) {
        return ResponseEntity.ok(
                containerTypeService.getContainerTypeById(containerTypeId)
        );
    }

    @PutMapping("/{containerTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContainerTypeResponse> updateContainerType(
            @PathVariable Long containerTypeId,
            @Valid @RequestBody ContainerTypeRequest request
    ) {
        return ResponseEntity.ok(
                containerTypeService.updateContainerType(
                        containerTypeId,
                        request
                )
        );
    }

    @DeleteMapping("/{containerTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContainerType(
            @PathVariable Long containerTypeId
    ) {
        containerTypeService.deleteContainerType(containerTypeId);

        return ResponseEntity.noContent().build();
    }
}