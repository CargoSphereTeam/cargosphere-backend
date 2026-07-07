package com.cargosphere.shipment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentHealthController.class)
class ShipmentHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthShouldReturnShipmentServiceStatus() throws Exception {
        mockMvc.perform(get("/api/shipments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("shipment-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}