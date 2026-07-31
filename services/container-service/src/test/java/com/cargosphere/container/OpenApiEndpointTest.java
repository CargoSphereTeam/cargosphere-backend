package com.cargosphere.container;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiSpecificationShouldBePubliclyAvailable()
            throws Exception {

        mockMvc.perform(
                        get("/v3/api-docs")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.info.title")
                                .value(
                                        "CargoSphere Container Service API"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/container-types']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/container-types/{containerTypeId}']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/container-allocations']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/container-allocations/{allocationId}']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/container-allocations/shipment/{shipmentId}']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['components']"
                                        + "['securitySchemes']"
                                        + "['bearerAuth']"
                        ).exists()
                );
    }
}
