package com.cargosphere.shipment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.flyway.enabled=false"
        }
)
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
                                        "CargoSphere Shipment Service API"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments/{shipmentId}']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments/number/{shipmentNumber}']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments/client/{clientUserId}']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments/{shipmentId}/cargo-details']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments/{shipmentId}/status']"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$['paths']"
                                        + "['/api/shipments/{shipmentId}/events']"
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
