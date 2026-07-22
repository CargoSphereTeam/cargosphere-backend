package com.cargosphere.container.service.impl;

import com.cargosphere.container.dto.AllocationRequest;
import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.entity.ContainerType;
import com.cargosphere.container.entity.ShipmentContainerAllocation;
import com.cargosphere.container.exception.DuplicateResourceException;
import com.cargosphere.container.exception.ResourceNotFoundException;
import com.cargosphere.container.mapper.ContainerMapper;
import com.cargosphere.container.repository.ContainerTypeRepository;
import com.cargosphere.container.repository.ShipmentContainerAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentContainerAllocationServiceImplTest {

    @Mock
    private ShipmentContainerAllocationRepository allocationRepository;

    @Mock
    private ContainerTypeRepository containerTypeRepository;

    @Mock
    private ContainerMapper containerMapper;

    @InjectMocks
    private ShipmentContainerAllocationServiceImpl allocationService;

    private ContainerType containerType;
    private ShipmentContainerAllocation allocation;
    private AllocationRequest request;
    private AllocationResponse response;

    @BeforeEach
    void setUp() {
        containerType = ContainerType.builder()
                .containerTypeId(1L)
                .typeCode("20GP")
                .typeName("20 Foot General Purpose")
                .active(true)
                .build();

        request = new AllocationRequest(
                10L,
                1L,
                2,
                "ALLOCATED",
                "Test allocation"
        );

        allocation = ShipmentContainerAllocation.builder()
                .allocationId(1L)
                .shipmentId(10L)
                .containerType(containerType)
                .quantity(2)
                .allocationStatus("ALLOCATED")
                .notes("Test allocation")
                .allocatedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        response = new AllocationResponse(
                1L,
                10L,
                1L,
                "20GP",
                "20 Foot General Purpose",
                2,
                "ALLOCATED",
                "Test allocation",
                allocation.getAllocatedAt(),
                allocation.getUpdatedAt()
        );
    }

    @Test
    void createAllocation_shouldSaveAndReturnResponse() {
        when(containerTypeRepository.findById(1L))
                .thenReturn(Optional.of(containerType));
        when(allocationRepository
                .existsByShipmentIdAndContainerTypeContainerTypeId(10L, 1L))
                .thenReturn(false);
        when(allocationRepository.save(any(ShipmentContainerAllocation.class)))
                .thenReturn(allocation);
        when(containerMapper.toAllocationResponse(allocation))
                .thenReturn(response);

        AllocationResponse actual =
                allocationService.createAllocation(request);

        assertNotNull(actual);
        assertEquals(1L, actual.allocationId());
        assertEquals(10L, actual.shipmentId());
        assertEquals("20GP", actual.containerTypeCode());

        verify(allocationRepository)
                .save(any(ShipmentContainerAllocation.class));
    }

    @Test
    void createAllocation_whenContainerTypeMissing_shouldThrowNotFound() {
        when(containerTypeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> allocationService.createAllocation(request)
        );

        verifyNoInteractions(allocationRepository);
    }

    @Test
    void createAllocation_whenDuplicate_shouldThrowConflict() {
        when(containerTypeRepository.findById(1L))
                .thenReturn(Optional.of(containerType));
        when(allocationRepository
                .existsByShipmentIdAndContainerTypeContainerTypeId(10L, 1L))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> allocationService.createAllocation(request)
        );

        verify(allocationRepository, never())
                .save(any(ShipmentContainerAllocation.class));
    }

    @Test
    void getAllocationById_whenFound_shouldReturnResponse() {
        when(allocationRepository.findById(1L))
                .thenReturn(Optional.of(allocation));
        when(containerMapper.toAllocationResponse(allocation))
                .thenReturn(response);

        AllocationResponse actual =
                allocationService.getAllocationById(1L);

        assertEquals(1L, actual.allocationId());
        assertEquals(10L, actual.shipmentId());
    }

    @Test
    void getAllocationById_whenMissing_shouldThrowNotFound() {
        when(allocationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> allocationService.getAllocationById(99L)
        );

        verifyNoInteractions(containerMapper);
    }

    @Test
    void getAllocationsByShipmentId_shouldReturnMappedList() {
        when(allocationRepository.findByShipmentId(10L))
                .thenReturn(List.of(allocation));
        when(containerMapper.toAllocationResponse(allocation))
                .thenReturn(response);

        List<AllocationResponse> actual =
                allocationService.getAllocationsByShipmentId(10L);

        assertEquals(1, actual.size());
        assertEquals(10L, actual.get(0).shipmentId());
    }

    @Test
    void deleteAllocation_whenFound_shouldDeleteEntity() {
        when(allocationRepository.findById(1L))
                .thenReturn(Optional.of(allocation));

        allocationService.deleteAllocation(1L);

        verify(allocationRepository).delete(allocation);
    }

    @Test
    void deleteAllocation_whenMissing_shouldThrowNotFound() {
        when(allocationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> allocationService.deleteAllocation(99L)
        );

        verify(allocationRepository, never()).delete(any());
    }
}