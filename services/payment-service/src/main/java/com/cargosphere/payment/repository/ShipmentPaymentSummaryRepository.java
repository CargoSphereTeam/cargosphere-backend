package com.cargosphere.payment.repository;

import com.cargosphere.payment.entity.ShipmentPaymentSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentPaymentSummaryRepository
        extends JpaRepository<ShipmentPaymentSummary, Long> {

    Optional<ShipmentPaymentSummary> findByShipmentId(Long shipmentId);

}