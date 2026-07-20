package com.cargosphere.shipment.repository;

import com.cargosphere.shipment.entity.Shipment;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByShipmentNumber(String shipmentNumber);

    boolean existsByShipmentNumber(String shipmentNumber);

    List<Shipment> findByClientUserId(Long clientUserId);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByShipmentType(ShipmentType shipmentType);

    List<Shipment> findByClientUserIdAndStatus(Long clientUserId, ShipmentStatus status);
}