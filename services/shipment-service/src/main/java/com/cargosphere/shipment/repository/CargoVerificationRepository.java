package com.cargosphere.shipment.repository;

import com.cargosphere.shipment.entity.CargoVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CargoVerificationRepository
        extends JpaRepository<CargoVerification, Long> {

    Optional<CargoVerification> findByCargoDetail_Id(
            Long cargoDetailId
    );

    List<CargoVerification>
    findByCargoDetail_Shipment_IdOrderByCargoDetail_IdAsc(
            Long shipmentId
    );

    List<CargoVerification> findByCargoDetail_IdIn(
            Collection<Long> cargoDetailIds
    );

    boolean existsByCargoDetail_Id(Long cargoDetailId);
}