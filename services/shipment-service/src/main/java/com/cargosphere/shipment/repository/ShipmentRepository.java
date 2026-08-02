package com.cargosphere.shipment.repository;

import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long>,
        JpaSpecificationExecutor<Shipment> {

    Optional<Shipment> findByShipmentNumber(
            String shipmentNumber
    );

    boolean existsByShipmentNumber(
            String shipmentNumber
    );

    List<Shipment> findByClientUserId(
            Long clientUserId
    );

    List<Shipment> findByStatus(
            ShipmentStatus status
    );

    List<Shipment> findByShipmentType(
            ShipmentType shipmentType
    );

    List<Shipment> findByClientUserIdAndStatus(
            Long clientUserId,
            ShipmentStatus status
    );

    boolean existsByIdAndClientUserId(
            Long id,
            Long clientUserId
    );

    boolean existsByShipmentNumberAndClientUserId(
            String shipmentNumber,
            Long clientUserId
    );

    Optional<Shipment> findByEbillNumber(
            String ebillNumber
    );

    boolean existsByEbillNumber(
            String ebillNumber
    );
}