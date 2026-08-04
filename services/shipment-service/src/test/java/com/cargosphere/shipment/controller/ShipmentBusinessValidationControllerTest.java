package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.dto.CargoDetailRequest;
import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.UpdateShipmentStatusRequest;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import com.cargosphere.shipment.exception.InvalidShipmentOperationException;
import com.cargosphere.shipment.service.ShipmentService;
import com.cargosphere.shipment.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.context.annotation.Import;

@WebMvcTest(ShipmentController.class)
@Import({
        SecurityConfig.class
})
@WithMockUser(
        username = "admin@cargosphere.com",
        roles = "ADMIN"
)
class ShipmentBusinessValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createShipmentShouldReturnBadRequestForInvalidDateOrder()
            throws Exception {

        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .expectedPickupDate(LocalDate.now().plusDays(10))
                .expectedDeliveryDate(LocalDate.now().plusDays(5))
                .build();

        when(shipmentService.createShipment(
                any(CreateShipmentRequest.class)
        )).thenThrow(new InvalidShipmentOperationException(
                "Expected delivery date cannot be before expected pickup date"
        ));

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Expected delivery date cannot be before expected pickup date"
                ))
                .andExpect(jsonPath("$.path").value("/api/shipments"));
    }

    @Test
    void updateShipmentStatusShouldReturnBadRequestForInvalidTransition()
            throws Exception {

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.IN_TRANSIT)
                        .build();

        when(shipmentService.updateShipmentStatus(
                eq(1L),
                any(UpdateShipmentStatusRequest.class)
        )).thenThrow(new InvalidShipmentOperationException(
                "Invalid shipment status transition from CREATED to IN_TRANSIT"
        ));

        mockMvc.perform(patch("/api/shipments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Invalid shipment status transition from CREATED to IN_TRANSIT"
                ))
                .andExpect(jsonPath("$.path")
                        .value("/api/shipments/1/status"));
    }

    @Test
    void addCargoDetailShouldReturnBadRequestWhenCargoAdditionIsBlocked()
            throws Exception {

        CargoDetailRequest request = CargoDetailRequest.builder()
                .cargoName("Electronics Box")
                .cargoType(CargoType.ELECTRONICS)
                .weightKg(new BigDecimal("25.500"))
                .volumeCbm(new BigDecimal("1.250"))
                .quantity(2)
                .fragile(true)
                .hazardous(false)
                .build();

        when(shipmentService.addCargoDetail(
                eq(1L),
                any(CargoDetailRequest.class)
        )).thenThrow(new InvalidShipmentOperationException(
                "Cargo cannot be added when shipment status is IN_TRANSIT"
        ));

        mockMvc.perform(post("/api/shipments/1/cargo-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Cargo cannot be added when shipment status is IN_TRANSIT"
                ))
                .andExpect(jsonPath("$.path")
                        .value("/api/shipments/1/cargo-details"));
    }

    @Test
    void createShipmentShouldReturnBadRequestForInvalidClientUserId()
            throws Exception {

        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .clientUserId(0L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .expectedPickupDate(LocalDate.now().plusDays(5))
                .expectedDeliveryDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath(
                        "$.validationErrors.clientUserId"
                ).value(
                        "Client user id must be greater than zero"
                ));
    }

    @Test
    void addCargoDetailShouldRejectWeightWithMoreThanThreeDecimalPlaces()
            throws Exception {

        CargoDetailRequest request = CargoDetailRequest.builder()
                .cargoName("Invalid Weight Precision Test")
                .cargoType(CargoType.GENERAL)
                .weightKg(new BigDecimal("25.5678"))
                .volumeCbm(new BigDecimal("1.257"))
                .quantity(1)
                .fragile(false)
                .hazardous(false)
                .build();

        mockMvc.perform(post("/api/shipments/1/cargo-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath(
                        "$.validationErrors.weightKg"
                ).value(
                        "Cargo weight can contain up to 10 integer digits and 3 decimal digits"
                ));
    }

    @Test
    void addCargoDetailShouldRejectVolumeWithMoreThanThreeDecimalPlaces()
            throws Exception {

        CargoDetailRequest request = CargoDetailRequest.builder()
                .cargoName("Invalid Volume Precision Test")
                .cargoType(CargoType.GENERAL)
                .weightKg(new BigDecimal("25.567"))
                .volumeCbm(new BigDecimal("1.2578"))
                .quantity(1)
                .fragile(false)
                .hazardous(false)
                .build();

        mockMvc.perform(post("/api/shipments/1/cargo-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath(
                        "$.validationErrors.volumeCbm"
                ).value(
                        "Cargo volume can contain up to 10 integer digits and 3 decimal digits"
                ));
    }
}
