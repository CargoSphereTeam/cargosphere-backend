package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.config.CorsConfig;
import com.cargosphere.shipment.dto.*;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import com.cargosphere.shipment.exception.ResourceNotFoundException;
import com.cargosphere.shipment.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@Import(CorsConfig.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    @Test
    void createShipmentShouldReturnCreatedShipment() throws Exception {
        CreateShipmentRequest request = CreateShipmentRequest.builder()
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .expectedPickupDate(LocalDate.of(2026, 8, 1))
                .expectedDeliveryDate(LocalDate.of(2026, 8, 5))
                .build();

        ShipmentResponse response = ShipmentResponse.builder()
                .id(1L)
                .shipmentNumber("CS-20260719-ABC12345")
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .status(ShipmentStatus.CREATED)
                .expectedPickupDate(LocalDate.of(2026, 8, 1))
                .expectedDeliveryDate(LocalDate.of(2026, 8, 5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(shipmentService.createShipment(any(CreateShipmentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.shipmentNumber")
                        .value("CS-20260719-ABC12345"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getShipmentByIdShouldReturnShipment() throws Exception {
        ShipmentResponse response = ShipmentResponse.builder()
                .id(1L)
                .shipmentNumber("CS-20260719-ABC12345")
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .status(ShipmentStatus.CREATED)
                .build();

        when(shipmentService.getShipmentById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/shipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.shipmentNumber")
                        .value("CS-20260719-ABC12345"));
    }

    @Test
    void getAllShipmentsShouldReturnShipmentList() throws Exception {
        ShipmentResponse response = ShipmentResponse.builder()
                .id(1L)
                .shipmentNumber("CS-20260719-ABC12345")
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .status(ShipmentStatus.CREATED)
                .build();

        when(shipmentService.getAllShipments())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void addCargoDetailShouldReturnCreatedCargoDetail() throws Exception {
        CargoDetailRequest request = CargoDetailRequest.builder()
                .cargoName("Electronics Box")
                .cargoDescription("Laptop accessories")
                .cargoType(CargoType.ELECTRONICS)
                .weightKg(BigDecimal.valueOf(25.50))
                .volumeCbm(BigDecimal.valueOf(1.20))
                .quantity(2)
                .fragile(true)
                .hazardous(false)
                .build();

        CargoDetailResponse response = CargoDetailResponse.builder()
                .id(1L)
                .shipmentId(1L)
                .cargoName("Electronics Box")
                .cargoDescription("Laptop accessories")
                .cargoType(CargoType.ELECTRONICS)
                .weightKg(BigDecimal.valueOf(25.50))
                .volumeCbm(BigDecimal.valueOf(1.20))
                .quantity(2)
                .fragile(true)
                .hazardous(false)
                .build();

        when(shipmentService.addCargoDetail(
                eq(1L),
                any(CargoDetailRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/shipments/1/cargo-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cargoName")
                        .value("Electronics Box"))
                .andExpect(jsonPath("$.cargoType")
                        .value("ELECTRONICS"));
    }

    @Test
    void updateShipmentStatusShouldReturnUpdatedShipment() throws Exception {
        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.IN_TRANSIT)
                        .build();

        ShipmentResponse response = ShipmentResponse.builder()
                .id(1L)
                .shipmentNumber("CS-20260719-ABC12345")
                .clientUserId(1L)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        when(shipmentService.updateShipmentStatus(
                eq(1L),
                any(UpdateShipmentStatusRequest.class)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/shipments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("IN_TRANSIT"));
    }

    @Test
    void getShipmentEventsShouldReturnEventList() throws Exception {
        ShipmentEventResponse response =
                ShipmentEventResponse.builder()
                        .id(1L)
                        .shipmentId(1L)
                        .eventType(ShipmentEventType.CREATED)
                        .eventDescription("Shipment created")
                        .eventLocation("Mumbai")
                        .eventTime(LocalDateTime.now())
                        .build();

        when(shipmentService.getShipmentEventsByShipmentId(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/shipments/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType")
                        .value("CREATED"))
                .andExpect(jsonPath("$[0].eventDescription")
                        .value("Shipment created"));
    }

    @Test
    void getShipmentByIdShouldReturnNotFoundWhenShipmentDoesNotExist()
            throws Exception {

        when(shipmentService.getShipmentById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Shipment not found with id: 999"
                ));

        mockMvc.perform(get("/api/shipments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Shipment not found with id: 999"));
    }

    @Test
    void createShipmentShouldReturnBadRequestForInvalidPayload()
            throws Exception {

        String invalidRequest = """
                {
                  "clientUserId": null,
                  "originLocation": "",
                  "destinationLocation": "",
                  "shipmentType": null
                }
                """;

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath(
                        "$.validationErrors.clientUserId"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.originLocation"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.destinationLocation"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.shipmentType"
                ).exists());
    }

    @Test
    void shipmentApiShouldAllowCargoSphereFrontendOrigin()
            throws Exception {

        mockMvc.perform(options("/api/shipments")
                        .header(
                                HttpHeaders.ORIGIN,
                                "http://localhost:5173"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "GET"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:5173"
                ));
    }
}