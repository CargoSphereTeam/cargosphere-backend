package com.cargosphere.container.controller;

import com.cargosphere.container.dto.ContainerTypeRequest;
import com.cargosphere.container.dto.ContainerTypeResponse;
import com.cargosphere.container.exception.DuplicateResourceException;
import com.cargosphere.container.exception.GlobalExceptionHandler;
import com.cargosphere.container.exception.ResourceNotFoundException;
import com.cargosphere.container.service.ContainerTypeService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContainerTypeController.class)
@Import(GlobalExceptionHandler.class)
class ContainerTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContainerTypeService containerTypeService;

    private ContainerTypeRequest validRequest;
    private ContainerTypeResponse response;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        validRequest = new ContainerTypeRequest(
                "20GP",
                "20 Foot General Purpose",
                "Standard dry cargo container",
                new BigDecimal("28000.00"),
                new BigDecimal("33.20"),
                new BigDecimal("6.06"),
                new BigDecimal("2.44"),
                new BigDecimal("2.59"),
                true
        );

        response = new ContainerTypeResponse(
                1L,
                "20GP",
                "20 Foot General Purpose",
                "Standard dry cargo container",
                new BigDecimal("28000.00"),
                new BigDecimal("33.20"),
                new BigDecimal("6.06"),
                new BigDecimal("2.44"),
                new BigDecimal("2.59"),
                true,
                now,
                now
        );
    }

    @Test
    void createContainerType_shouldReturn201() throws Exception {
        when(containerTypeService.createContainerType(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/container-types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.containerTypeId").value(1))
                .andExpect(jsonPath("$.typeCode").value("20GP"))
                .andExpect(jsonPath("$.typeName")
                        .value("20 Foot General Purpose"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createContainerType_whenInvalid_shouldReturn400() throws Exception {
        ContainerTypeRequest invalidRequest =
                new ContainerTypeRequest(
                        "",
                        "",
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        true
                );

        mockMvc.perform(
                        post("/api/container-types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        invalidRequest
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath(
                        "$.validationErrors.typeCode"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.typeName"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.maxWeightKg"
                ).exists())
                .andExpect(jsonPath(
                        "$.validationErrors.maxVolumeCbm"
                ).exists());
    }

    @Test
    void createContainerType_whenDuplicate_shouldReturn409()
            throws Exception {

        when(containerTypeService.createContainerType(any()))
                .thenThrow(
                        new DuplicateResourceException(
                                "Container type already exists with code: 20GP"
                        )
                );

        mockMvc.perform(
                        post("/api/container-types")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        validRequest
                                ))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Container type already exists with code: 20GP"
                        ));
    }

    @Test
    void getAllContainerTypes_shouldReturn200() throws Exception {
        when(containerTypeService.getAllContainerTypes())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/container-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].containerTypeId").value(1))
                .andExpect(jsonPath("$[0].typeCode").value("20GP"));
    }

    @Test
    void getContainerTypeById_shouldReturn200() throws Exception {
        when(containerTypeService.getContainerTypeById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/container-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerTypeId").value(1))
                .andExpect(jsonPath("$.typeCode").value("20GP"));
    }

    @Test
    void getContainerTypeById_whenMissing_shouldReturn404()
            throws Exception {

        when(containerTypeService.getContainerTypeById(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Container type not found with ID: 99"
                        )
                );

        mockMvc.perform(get("/api/container-types/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Container type not found with ID: 99"));
    }

    @Test
    void updateContainerType_shouldReturn200() throws Exception {
        when(containerTypeService.updateContainerType(
                any(Long.class),
                any(ContainerTypeRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/container-types/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        validRequest
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.containerTypeId").value(1))
                .andExpect(jsonPath("$.typeCode").value("20GP"));
    }

    @Test
    void deleteContainerType_shouldReturn204() throws Exception {
        doNothing()
                .when(containerTypeService)
                .deleteContainerType(1L);

        mockMvc.perform(delete("/api/container-types/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}