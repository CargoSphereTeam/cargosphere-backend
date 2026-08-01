package com.cargosphere.shipment.repository;

import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.enums.CargoType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CargoDetailRepository extends JpaRepository<CargoDetail, Long> {

    List<CargoDetail> findByShipment_Id(Long shipmentId);

    List<CargoDetail> findByCargoType(CargoType cargoType);

    List<CargoDetail> findByFragileTrue();

    List<CargoDetail> findByHazardousTrue();
}