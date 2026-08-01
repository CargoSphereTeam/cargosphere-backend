package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.config.CorsConfig;
import com.cargosphere.shipment.config.SecurityConfig;
import com.cargosphere.shipment.dto.CargoDetailRequest;
import com.cargosphere.shipment.dto.CargoDetailResponse;
import com.cargosphere.shipment.dto.CreateShipmentRequest;
import com.cargosphere.shipment.dto.ShipmentEventResponse;
import com.cargosphere.shipment.dto.ShipmentResponse;
import com.cargosphere.shipment.dto.UpdateShipmentStatusRequest;
import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import com.cargosphere.shipment.exception.ResourceNotFoundException;
import com.cargosphere.shipment.security.ShipmentAuthorizationService;
import com.cargosphere.shipment.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
@Import({
        CorsConfig.class,
        SecurityConfig.class
})
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean(name = "shipmentAuthorizationService")
    private ShipmentAuthorizationService shipmentAuthorizationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldCreateOwnShipment() throws Exception {
        CreateShipmentRequest request = createShipmentRequest(1L);

        OffsetDateTime timestamp = OffsetDateTime.of(
                2026,
                7,
                19,
                10,
                30,
                0,
                0,
                ZoneOffset.UTC
        );

        ShipmentResponse response = createShipmentResponse(
                1L,
                1L,
                ShipmentStatus.CREATED,
                timestamp
        );

        when(
                shipmentAuthorizationService.isCurrentUser(
                        eq(1L),
                        any(Authentication.class)
                )
        ).thenReturn(true);

        when(
                shipmentService.createShipment(
                        any(CreateShipmentRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.shipmentNumber")
                                .value("CS-20260719-ABC12345")
                )
                .andExpect(jsonPath("$.clientUserId").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(
                        jsonPath("$.createdAt")
                                .value("2026-07-19T10:30:00Z")
                )
                .andExpect(
                        jsonPath("$.updatedAt")
                                .value("2026-07-19T10:30:00Z")
                );
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotCreateShipmentForAnotherUser()
            throws Exception {

        CreateShipmentRequest request = createShipmentRequest(99L);

        when(
                shipmentAuthorizationService.isCurrentUser(
                        eq(99L),
                        any(Authentication.class)
                )
        ).thenReturn(false);

        mockMvc.perform(
                        post("/api/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientOwnerShouldGetShipmentById() throws Exception {
        ShipmentResponse response = createShipmentResponse(
                1L,
                1L,
                ShipmentStatus.CREATED,
                null
        );

        when(
                shipmentAuthorizationService.isShipmentOwner(
                        eq(1L),
                        any(Authentication.class)
                )
        ).thenReturn(true);

        when(shipmentService.getShipmentById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/shipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.shipmentNumber")
                                .value("CS-20260719-ABC12345")
                );
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientNonOwnerShouldNotGetShipmentById()
            throws Exception {

        when(
                shipmentAuthorizationService.isShipmentOwner(
                        eq(1L),
                        any(Authentication.class)
                )
        ).thenReturn(false);

        mockMvc.perform(get("/api/shipments/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    @WithMockUser(
            username = "admin@cargosphere.com",
            roles = "ADMIN"
    )
    void adminShouldGetAllShipments() throws Exception {
        ShipmentResponse response = createShipmentResponse(
                1L,
                1L,
                ShipmentStatus.CREATED,
                null
        );

        when(shipmentService.getAllShipments())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].status")
                                .value("CREATED")
                );
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotGetAllShipments() throws Exception {
        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    void unauthenticatedUserShouldNotAccessShipments()
            throws Exception {

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shipmentService);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void shipmentOwnerShouldAddCargoDetail()
            throws Exception {

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

        when(
                shipmentAuthorizationService.isShipmentOwner(
                        eq(1L),
                        any(Authentication.class)
                )
        ).thenReturn(true);

        when(
                shipmentService.addCargoDetail(
                        eq(1L),
                        any(CargoDetailRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/shipments/1/cargo-details")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.cargoName")
                                .value("Electronics Box")
                )
                .andExpect(
                        jsonPath("$.cargoType")
                                .value("ELECTRONICS")
                );
    }

    @Test
    @WithMockUser(
            username = "admin@cargosphere.com",
            roles = "ADMIN"
    )
    void adminShouldUpdateShipmentStatus()
            throws Exception {

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.IN_TRANSIT)
                        .build();

        ShipmentResponse response = createShipmentResponse(
                1L,
                1L,
                ShipmentStatus.IN_TRANSIT,
                null
        );

        when(
                shipmentService.updateShipmentStatus(
                        eq(1L),
                        any(UpdateShipmentStatusRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch("/api/shipments/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_TRANSIT")
                );
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotUpdateShipmentStatus()
            throws Exception {

        UpdateShipmentStatusRequest request =
                UpdateShipmentStatusRequest.builder()
                        .status(ShipmentStatus.IN_TRANSIT)
                        .build();

        mockMvc.perform(
                        patch("/api/shipments/1/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void shipmentOwnerShouldGetShipmentEvents()
            throws Exception {

        ShipmentEventResponse response =
                ShipmentEventResponse.builder()
                        .id(1L)
                        .shipmentId(1L)
                        .eventType(ShipmentEventType.CREATED)
                        .eventDescription("Shipment created")
                        .eventLocation("Mumbai")
                        .eventTime(LocalDateTime.now())
                        .build();

        when(
                shipmentAuthorizationService.isShipmentOwner(
                        eq(1L),
                        any(Authentication.class)
                )
        ).thenReturn(true);

        when(
                shipmentService
                        .getShipmentEventsByShipmentId(1L)
        ).thenReturn(List.of(response));

        mockMvc.perform(get("/api/shipments/1/events"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].eventType")
                                .value("CREATED")
                )
                .andExpect(
                        jsonPath("$[0].eventDescription")
                                .value("Shipment created")
                );
    }

    @Test
    @WithMockUser(
            username = "admin@cargosphere.com",
            roles = "ADMIN"
    )
    void getShipmentByIdShouldReturnNotFoundWhenShipmentDoesNotExist()
            throws Exception {

        when(shipmentService.getShipmentById(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Shipment not found with id: 999"
                        )
                );

        mockMvc.perform(get("/api/shipments/999"))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Shipment not found with id: 999"
                                )
                );
    }

    @Test
    @WithMockUser(
            username = "admin@cargosphere.com",
            roles = "ADMIN"
    )
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

        mockMvc.perform(
                        post("/api/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.clientUserId"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.originLocation"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.destinationLocation"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.shipmentType"
                        ).exists()
                );
    }

    @Test
    void shipmentApiShouldAllowCargoSphereFrontendOrigin()
            throws Exception {

        mockMvc.perform(
                        options("/api/shipments")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://localhost:5173"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                "http://localhost:5173"
                        )
                );
    }

    private CreateShipmentRequest createShipmentRequest(
            Long clientUserId
    ) {
        return CreateShipmentRequest.builder()
                .clientUserId(clientUserId)
                .originLocation("Mumbai")
                .destinationLocation("Pune")
                .shipmentType(ShipmentType.ROAD)
                .expectedPickupDate(LocalDate.of(2026, 8, 1))
                .expectedDeliveryDate(LocalDate.of(2026, 8, 5))
                .build();
    }

    private ShipmentResponse createShipmentResponse(
            Long shipmentId,
            Long clientUserId,
            ShipmentStatus status,
            OffsetDateTime timestamp
    ) {
        ShipmentResponse.ShipmentResponseBuilder builder =
                ShipmentResponse.builder()
                        .id(shipmentId)
                        .shipmentNumber("CS-20260719-ABC12345")
                        .clientUserId(clientUserId)
                        .originLocation("Mumbai")
                        .destinationLocation("Pune")
                        .shipmentType(ShipmentType.ROAD)
                        .status(status)
                        .expectedPickupDate(
                                LocalDate.of(2026, 8, 1)
                        )
                        .expectedDeliveryDate(
                                LocalDate.of(2026, 8, 5)
                        );

        if (timestamp != null) {
            builder.createdAt(timestamp);
            builder.updatedAt(timestamp);
        }

        return builder.build();
    }
}