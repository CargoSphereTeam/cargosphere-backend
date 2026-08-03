package com.cargosphere.shipment.mapper;

import com.cargosphere.shipment.dto.admin.CargoVerificationItemRequest;
import com.cargosphere.shipment.dto.admin.CargoVerificationItemResponse;
import com.cargosphere.shipment.entity.CargoDetail;
import com.cargosphere.shipment.entity.CargoVerification;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CargoVerificationMapperTest {

    private CargoVerificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CargoVerificationMapper();
    }

    @Test
    void shouldCreateDraftVerificationFromRequest() {
        CargoDetail cargoDetail = CargoDetail.builder()
                .id(11L)
                .build();

        CargoVerificationItemRequest request =
                CargoVerificationItemRequest.builder()
                        .cargoDetailId(11L)
                        .confirmedCargoName("Electronics Box")
                        .confirmedCargoDescription(
                                "Verified laptop accessories"
                        )
                        .confirmedCargoType(CargoType.ELECTRONICS)
                        .confirmedWeightKg(
                                new BigDecimal("25.500")
                        )
                        .confirmedVolumeCbm(
                                new BigDecimal("1.200")
                        )
                        .confirmedQuantity(2)
                        .confirmedFragile(true)
                        .confirmedHazardous(false)
                        .verificationRemarks(
                                "Physical cargo measurements verified"
                        )
                        .build();

        CargoVerification result =
                mapper.toNewEntity(cargoDetail, request);

        assertThat(result).isNotNull();
        assertThat(result.getCargoDetail())
                .isSameAs(cargoDetail);
        assertThat(result.getVerificationStatus())
                .isEqualTo(CargoVerificationStatus.DRAFT);
        assertThat(result.getConfirmedCargoName())
                .isEqualTo("Electronics Box");
        assertThat(result.getConfirmedCargoDescription())
                .isEqualTo("Verified laptop accessories");
        assertThat(result.getConfirmedCargoType())
                .isEqualTo(CargoType.ELECTRONICS);
        assertThat(result.getConfirmedWeightKg())
                .isEqualByComparingTo("25.500");
        assertThat(result.getConfirmedVolumeCbm())
                .isEqualByComparingTo("1.200");
        assertThat(result.getConfirmedQuantity())
                .isEqualTo(2);
        assertThat(result.getConfirmedFragile())
                .isTrue();
        assertThat(result.getConfirmedHazardous())
                .isFalse();
        assertThat(result.getVerificationRemarks())
                .isEqualTo(
                        "Physical cargo measurements verified"
                );
    }

    @Test
    void shouldUpdateExistingVerificationFromRequest() {
        CargoVerification verification =
                CargoVerification.builder()
                        .verificationStatus(
                                CargoVerificationStatus.DRAFT
                        )
                        .confirmedCargoName("Old Name")
                        .build();

        CargoVerificationItemRequest request =
                CargoVerificationItemRequest.builder()
                        .cargoDetailId(11L)
                        .confirmedCargoName("Updated Name")
                        .confirmedWeightKg(
                                new BigDecimal("30.000")
                        )
                        .confirmedQuantity(3)
                        .confirmedFragile(false)
                        .confirmedHazardous(true)
                        .verificationRemarks("Updated after inspection")
                        .build();

        mapper.updateEntity(verification, request);

        assertThat(verification.getConfirmedCargoName())
                .isEqualTo("Updated Name");
        assertThat(verification.getConfirmedWeightKg())
                .isEqualByComparingTo("30.000");
        assertThat(verification.getConfirmedQuantity())
                .isEqualTo(3);
        assertThat(verification.getConfirmedFragile())
                .isFalse();
        assertThat(verification.getConfirmedHazardous())
                .isTrue();
        assertThat(verification.getVerificationRemarks())
                .isEqualTo("Updated after inspection");
    }

    @Test
    void shouldMapEntityToResponse() {
        LocalDateTime verifiedAt =
                LocalDateTime.of(2026, 8, 2, 12, 0);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 2, 11, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 2, 12, 0);

        CargoDetail cargoDetail = CargoDetail.builder()
                .id(11L)
                .build();

        CargoVerification verification =
                CargoVerification.builder()
                        .id(21L)
                        .cargoDetail(cargoDetail)
                        .verificationStatus(
                                CargoVerificationStatus.CONFIRMED
                        )
                        .confirmedCargoName("Electronics Box")
                        .confirmedCargoDescription(
                                "Verified laptop accessories"
                        )
                        .confirmedCargoType(CargoType.ELECTRONICS)
                        .confirmedWeightKg(
                                new BigDecimal("25.500")
                        )
                        .confirmedVolumeCbm(
                                new BigDecimal("1.200")
                        )
                        .confirmedQuantity(2)
                        .confirmedFragile(true)
                        .confirmedHazardous(false)
                        .verificationRemarks(
                                "Physical cargo measurements verified"
                        )
                        .verifiedBy(5L)
                        .verifiedAt(verifiedAt)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

        CargoVerificationItemResponse response =
                mapper.toResponse(verification);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(21L);
        assertThat(response.getCargoDetailId()).isEqualTo(11L);
        assertThat(response.getVerificationStatus())
                .isEqualTo(CargoVerificationStatus.CONFIRMED);
        assertThat(response.getConfirmedCargoName())
                .isEqualTo("Electronics Box");
        assertThat(response.getVerifiedBy()).isEqualTo(5L);
        assertThat(response.getVerifiedAt())
                .isEqualTo(verifiedAt);
        assertThat(response.getCreatedAt())
                .isEqualTo(createdAt);
        assertThat(response.getUpdatedAt())
                .isEqualTo(updatedAt);
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertThat(mapper.toNewEntity(null, null))
                .isNull();

        assertThat(mapper.toResponse(null))
                .isNull();
    }
}