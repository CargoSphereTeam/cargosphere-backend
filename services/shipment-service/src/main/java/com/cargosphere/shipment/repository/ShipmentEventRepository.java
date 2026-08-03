package com.cargosphere.shipment.repository;

import com.cargosphere.shipment.entity.ShipmentEvent;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentEventRepository
        extends JpaRepository<ShipmentEvent, Long> {

    List<ShipmentEvent> findByShipment_Id(Long shipmentId);

    List<ShipmentEvent> findByShipment_IdOrderByEventTimeDesc(
            Long shipmentId
    );

    List<ShipmentEvent> findByShipment_IdAndEventType(
            Long shipmentId,
            ShipmentEventType eventType
    );

    List<ShipmentEvent> findByEventType(
            ShipmentEventType eventType
    );

    boolean existsByShipment_IdAndEventType(
            Long shipmentId,
            ShipmentEventType eventType
    );
}