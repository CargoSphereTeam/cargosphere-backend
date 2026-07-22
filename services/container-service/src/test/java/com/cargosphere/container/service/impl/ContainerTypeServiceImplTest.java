package com.cargosphere.container.service.impl;

import com.cargosphere.container.dto.ContainerTypeRequest;
import com.cargosphere.container.dto.ContainerTypeResponse;
import com.cargosphere.container.entity.ContainerType;
import com.cargosphere.container.exception.DuplicateResourceException;
import com.cargosphere.container.exception.ResourceNotFoundException;
import com.cargosphere.container.mapper.ContainerMapper;
import com.cargosphere.container.repository.ContainerTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerTypeServiceImplTest {

    @Mock
    private ContainerTypeRepository containerTypeRepository;

    @Mock
    private ContainerMapper containerMapper;

    @InjectMocks
    private ContainerTypeServiceImpl containerTypeService;

    private ContainerTypeRequest request;
    private ContainerType entity;
    private ContainerTypeResponse response;

    @BeforeEach
    void setUp() {
        request = new ContainerTypeRequest(
                "20gp",
                "20 Foot General Purpose",
                "Standard dry cargo container",
                new BigDecimal("28000.00"),
                new BigDecimal("33.20"),
                new BigDecimal("6.06"),
                new BigDecimal("2.44"),
                new BigDecimal("2.59"),
                true
        );

        entity = ContainerType.builder()
                .containerTypeId(1L)
                .typeCode("20GP")
                .typeName("20 Foot General Purpose")
                .description("Standard dry cargo container")
                .maxWeightKg(new BigDecimal("28000.00"))
                .maxVolumeCbm(new BigDecimal("33.20"))
                .lengthM(new BigDecimal("6.06"))
                .widthM(new BigDecimal("2.44"))
                .heightM(new BigDecimal("2.59"))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

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
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Test
    void createContainerType_shouldSaveAndReturnResponse() {
        when(containerTypeRepository.existsByTypeCodeIgnoreCase("20GP"))
                .thenReturn(false);
        when(containerTypeRepository.save(any(ContainerType.class)))
                .thenReturn(entity);
        when(containerMapper.toContainerTypeResponse(entity))
                .thenReturn(response);

        ContainerTypeResponse actual =
                containerTypeService.createContainerType(request);

        assertNotNull(actual);
        assertEquals(1L, actual.containerTypeId());
        assertEquals("20GP", actual.typeCode());

        verify(containerTypeRepository)
                .existsByTypeCodeIgnoreCase("20GP");
        verify(containerTypeRepository).save(any(ContainerType.class));
        verify(containerMapper).toContainerTypeResponse(entity);
    }

    @Test
    void createContainerType_whenCodeExists_shouldThrowConflict() {
        when(containerTypeRepository.existsByTypeCodeIgnoreCase("20GP"))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> containerTypeService.createContainerType(request)
        );

        assertTrue(exception.getMessage().contains("20GP"));

        verify(containerTypeRepository, never())
                .save(any(ContainerType.class));
        verifyNoInteractions(containerMapper);
    }

    @Test
    void getContainerTypeById_whenFound_shouldReturnResponse() {
        when(containerTypeRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(containerMapper.toContainerTypeResponse(entity))
                .thenReturn(response);

        ContainerTypeResponse actual =
                containerTypeService.getContainerTypeById(1L);

        assertEquals(1L, actual.containerTypeId());
        assertEquals("20GP", actual.typeCode());
    }

    @Test
    void getContainerTypeById_whenMissing_shouldThrowNotFound() {
        when(containerTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> containerTypeService.getContainerTypeById(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
        verifyNoInteractions(containerMapper);
    }

    @Test
    void getAllContainerTypes_shouldReturnMappedList() {
        when(containerTypeRepository.findAll())
                .thenReturn(List.of(entity));
        when(containerMapper.toContainerTypeResponse(entity))
                .thenReturn(response);

        List<ContainerTypeResponse> actual =
                containerTypeService.getAllContainerTypes();

        assertEquals(1, actual.size());
        assertEquals("20GP", actual.getFirst().typeCode());
    }

    @Test
    void updateContainerType_whenFound_shouldUpdateAndReturnResponse() {
        when(containerTypeRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(containerTypeRepository.findByTypeCodeIgnoreCase("20GP"))
                .thenReturn(Optional.of(entity));
        when(containerTypeRepository.save(entity))
                .thenReturn(entity);
        when(containerMapper.toContainerTypeResponse(entity))
                .thenReturn(response);

        ContainerTypeResponse actual =
                containerTypeService.updateContainerType(1L, request);

        assertEquals("20GP", actual.typeCode());
        verify(containerTypeRepository).save(entity);
    }

    @Test
    void deleteContainerType_whenFound_shouldDeleteEntity() {
        when(containerTypeRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        containerTypeService.deleteContainerType(1L);

        verify(containerTypeRepository).delete(entity);
    }

    @Test
    void deleteContainerType_whenMissing_shouldThrowNotFound() {
        when(containerTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> containerTypeService.deleteContainerType(99L)
        );

        verify(containerTypeRepository, never()).delete(any());
    }
}