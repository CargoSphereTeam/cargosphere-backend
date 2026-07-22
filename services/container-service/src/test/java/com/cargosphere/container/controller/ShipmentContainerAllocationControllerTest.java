package com.cargosphere.container.controller;

import com.cargosphere.container.config.SecurityConfig;
import com.cargosphere.container.dto.AllocationRequest;
import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.exception.DuplicateResourceException;
import com.cargosphere.container.exception.GlobalExceptionHandler;
import com.cargosphere.container.exception.ResourceNotFoundException;
import com.cargosphere.container.service.ShipmentContainerAllocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentContainerAllocationController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class
})
@WithMockUser(
        username = "admin@cargosphere.com",
        roles = "ADMIN"
)
class ShipmentContainerAllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentContainerAllocationService allocationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private AllocationRequest validRequest;
    private AllocationResponse response;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        validRequest = new AllocationRequest(
                10L,
                1L,
                2,
                "ALLOCATED",
                "Controller test allocation"
        );

        response = new AllocationResponse(
                1L,
                10L,
                1L,
                "20GP",
                "20 Foot General Purpose",
                2,
                "ALLOCATED",
                "Controller test allocation",
                now,
                now
        );
    }

    @Test
    void createAllocationShouldReturn201() throws Exception {
        when(allocationService.createAllocation(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/container-allocations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.allocationId").value(1))
                .andExpect(jsonPath("$.shipmentId").value(10))
                .andExpect(jsonPath("$.containerTypeId").value(1))
                .andExpect(
                        jsonPath("$.containerTypeCode")
                                .value("20GP")
                )
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(
                        jsonPath("$.allocationStatus")
                                .value("ALLOCATED")
                );
    }

    @Test
    void createAllocationWhenInvalidShouldReturn400()
            throws Exception {

        AllocationRequest invalidRequest =
                new AllocationRequest(
                        null,
                        null,
                        0,
                        null,
                        null
                );

        mockMvc.perform(
                        post("/api/container-allocations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                invalidRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.shipmentId"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.containerTypeId"
                        ).exists()
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors.quantity"
                        ).exists()
                );
    }

    @Test
    void createAllocationWhenDuplicateShouldReturn409()
            throws Exception {

        when(allocationService.createAllocation(any()))
                .thenThrow(
                        new DuplicateResourceException(
                                "Allocation already exists for shipment ID 10 "
                                        + "and container type ID 1"
                        )
                );

        mockMvc.perform(
                        post("/api/container-allocations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                );
    }

    @Test
    void getAllAllocationsShouldReturn200() throws Exception {
        when(allocationService.getAllAllocations())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/container-allocations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].allocationId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].shipmentId")
                                .value(10)
                );
    }

    @Test
    void getAllocationByIdShouldReturn200() throws Exception {
        when(allocationService.getAllocationById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/container-allocations/1"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.allocationId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.containerTypeCode")
                                .value("20GP")
                );
    }

    @Test
    void getAllocationByIdWhenMissingShouldReturn404()
            throws Exception {

        when(allocationService.getAllocationById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Allocation not found with ID: 99"
                        )
                );

        mockMvc.perform(get("/api/container-allocations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Allocation not found with ID: 99"
                                )
                );
    }

    @Test
    void getAllocationsByShipmentIdShouldReturn200()
            throws Exception {

        when(allocationService.getAllocationsByShipmentId(10L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get(
                                "/api/container-allocations/shipment/10"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].shipmentId")
                                .value(10)
                );
    }

    @Test
    void updateAllocationShouldReturn200() throws Exception {
        when(
                allocationService.updateAllocation(
                        any(Long.class),
                        any(AllocationRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/api/container-allocations/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.allocationId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(2)
                );
    }

    @Test
    void deleteAllocationShouldReturn204() throws Exception {
        doNothing()
                .when(allocationService)
                .deleteAllocation(1L);

        mockMvc.perform(
                        delete("/api/container-allocations/1")
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotAccessContainerAllocations()
            throws Exception {

        mockMvc.perform(get("/api/container-allocations"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(allocationService);
    }

    @Test
    @WithMockUser(
            username = "client@example.com",
            roles = "CLIENT"
    )
    void clientShouldNotCreateContainerAllocation()
            throws Exception {

        mockMvc.perform(
                        post("/api/container-allocations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                validRequest
                                        )
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(allocationService);
    }

    @Test
    @WithAnonymousUser
    void unauthenticatedUserShouldNotAccessContainerAllocations()
            throws Exception {

        mockMvc.perform(get("/api/container-allocations"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(allocationService);
    }
}