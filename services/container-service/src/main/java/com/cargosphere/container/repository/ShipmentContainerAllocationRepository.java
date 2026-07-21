package com.cargosphere.container.repository;

import com.cargosphere.container.entity.ShipmentContainerAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentContainerAllocationRepository
        extends JpaRepository<ShipmentContainerAllocation, Long> {

    List<ShipmentContainerAllocation> findByShipmentId(Long shipmentId);

    Optional<ShipmentContainerAllocation>
    findByShipmentIdAndContainerTypeContainerTypeId(
            Long shipmentId,
            Long containerTypeId
    );

    boolean existsByShipmentIdAndContainerTypeContainerTypeId(
            Long shipmentId,
            Long containerTypeId
    );
}