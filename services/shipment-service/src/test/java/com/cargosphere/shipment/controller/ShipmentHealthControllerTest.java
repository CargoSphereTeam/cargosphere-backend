package com.cargosphere.shipment.controller;

import com.cargosphere.shipment.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentHealthController.class)
@Import(SecurityConfig.class)
class ShipmentHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void healthShouldReturnShipmentServiceStatus()
            throws Exception {

        mockMvc.perform(get("/api/shipments/health"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.service")
                                .value("shipment-service")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                );
    }
}