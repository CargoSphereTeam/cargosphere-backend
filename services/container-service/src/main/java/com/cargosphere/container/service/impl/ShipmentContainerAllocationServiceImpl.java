package com.cargosphere.container.service.impl;

import com.cargosphere.container.audit.ContainerAuditPublisher;
import com.cargosphere.container.dto.AllocationRequest;
import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.entity.ContainerType;
import com.cargosphere.container.entity.ShipmentContainerAllocation;
import com.cargosphere.container.exception.DuplicateResourceException;
import com.cargosphere.container.exception.ResourceNotFoundException;
import com.cargosphere.container.mapper.ContainerMapper;
import com.cargosphere.container.repository.ContainerTypeRepository;
import com.cargosphere.container.repository.ShipmentContainerAllocationRepository;
import com.cargosphere.container.service.ShipmentContainerAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentContainerAllocationServiceImpl
        implements ShipmentContainerAllocationService {

    private final ShipmentContainerAllocationRepository allocationRepository;

    private final ContainerTypeRepository containerTypeRepository;

    private final ContainerMapper containerMapper;

    private final ContainerAuditPublisher auditPublisher;

    @Override
    public AllocationResponse createAllocation(
            AllocationRequest request
    ) {
        ContainerType containerType =
                findContainerTypeById(request.containerTypeId());

        boolean alreadyExists =
                allocationRepository
                        .existsByShipmentIdAndContainerTypeContainerTypeId(
                                request.shipmentId(),
                                request.containerTypeId()
                        );

        if (alreadyExists) {
            throw new DuplicateResourceException(
                    "Allocation already exists for shipment ID "
                            + request.shipmentId()
                            + " and container type ID "
                            + request.containerTypeId()
            );
        }

        ShipmentContainerAllocation allocation =
                ShipmentContainerAllocation.builder()
                        .shipmentId(request.shipmentId())
                        .containerType(containerType)
                        .quantity(request.quantity())
                        .allocationStatus(
                                normalizeStatus(
                                        request.allocationStatus()
                                )
                        )
                        .notes(trimToNull(request.notes()))
                        .build();

        ShipmentContainerAllocation savedAllocation =
                allocationRepository.save(allocation);

        AllocationResponse response =
                containerMapper.toAllocationResponse(
                        savedAllocation
                );

        auditPublisher.publishAllocated(response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllAllocations() {
        return allocationRepository.findAll()
                .stream()
                .map(containerMapper::toAllocationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResponse getAllocationById(
            Long allocationId
    ) {
        ShipmentContainerAllocation allocation =
                findAllocationById(allocationId);

        return containerMapper.toAllocationResponse(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsByShipmentId(
            Long shipmentId
    ) {
        return allocationRepository.findByShipmentId(shipmentId)
                .stream()
                .map(containerMapper::toAllocationResponse)
                .toList();
    }

    @Override
    public AllocationResponse updateAllocation(
            Long allocationId,
            AllocationRequest request
    ) {
        ShipmentContainerAllocation allocation =
                findAllocationById(allocationId);

        ContainerType containerType =
                findContainerTypeById(request.containerTypeId());

        allocationRepository
                .findByShipmentIdAndContainerTypeContainerTypeId(
                        request.shipmentId(),
                        request.containerTypeId()
                )
                .filter(existing ->
                        !existing.getAllocationId().equals(
                                allocationId
                        )
                )
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Allocation already exists for shipment ID "
                                    + request.shipmentId()
                                    + " and container type ID "
                                    + request.containerTypeId()
                    );
                });

        allocation.setShipmentId(request.shipmentId());
        allocation.setContainerType(containerType);
        allocation.setQuantity(request.quantity());
        allocation.setAllocationStatus(
                normalizeStatus(request.allocationStatus())
        );
        allocation.setNotes(trimToNull(request.notes()));

        ShipmentContainerAllocation updatedAllocation =
                allocationRepository.save(allocation);

        return containerMapper.toAllocationResponse(
                updatedAllocation
        );
    }

    @Override
    public void deleteAllocation(Long allocationId) {
        ShipmentContainerAllocation allocation =
                findAllocationById(allocationId);

        Long shipmentId = allocation.getShipmentId();

        allocationRepository.delete(allocation);

        auditPublisher.publishReleased(
                allocationId,
                shipmentId
        );
    }

    private ShipmentContainerAllocation findAllocationById(
            Long allocationId
    ) {
        return allocationRepository.findById(allocationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Allocation not found with ID: "
                                        + allocationId
                        )
                );
    }

    private ContainerType findContainerTypeById(
            Long containerTypeId
    ) {
        return containerTypeRepository.findById(containerTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Container type not found with ID: "
                                        + containerTypeId
                        )
                );
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ALLOCATED";
        }

        return status.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}