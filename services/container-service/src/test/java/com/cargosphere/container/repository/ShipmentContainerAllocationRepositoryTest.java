package com.cargosphere.container.repository;

import com.cargosphere.container.entity.ContainerType;
import com.cargosphere.container.entity.ShipmentContainerAllocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ShipmentContainerAllocationRepositoryTest {

    @Autowired
    private ShipmentContainerAllocationRepository allocationRepository;

    @Autowired
    private ContainerTypeRepository containerTypeRepository;

    private ContainerType containerType;

    @BeforeEach
    void setUp() {
        containerType = containerTypeRepository.saveAndFlush(
                createContainerType("ALLOC-" + shortId())
        );
    }

    @Test
    void save_shouldPersistAllocation() {
        ShipmentContainerAllocation allocation =
                createAllocation(1001L, containerType, 2);

        ShipmentContainerAllocation saved =
                allocationRepository.saveAndFlush(allocation);

        assertNotNull(saved.getAllocationId());
        assertEquals(1001L, saved.getShipmentId());
        assertEquals(2, saved.getQuantity());
        assertEquals("ALLOCATED", saved.getAllocationStatus());
        assertEquals(
                containerType.getContainerTypeId(),
                saved.getContainerType().getContainerTypeId()
        );
        assertNotNull(saved.getAllocatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void findByShipmentId_shouldReturnShipmentAllocations() {
        allocationRepository.saveAndFlush(
                createAllocation(2001L, containerType, 3)
        );

        List<ShipmentContainerAllocation> results =
                allocationRepository.findByShipmentId(2001L);

        assertEquals(1, results.size());
        assertEquals(2001L, results.get(0).getShipmentId());
        assertEquals(3, results.get(0).getQuantity());
    }

    @Test
    void findByShipmentIdAndContainerTypeId_shouldFindAllocation() {
        ShipmentContainerAllocation saved =
                allocationRepository.saveAndFlush(
                        createAllocation(3001L, containerType, 1)
                );

        Optional<ShipmentContainerAllocation> result =
                allocationRepository
                        .findByShipmentIdAndContainerTypeContainerTypeId(
                                3001L,
                                containerType.getContainerTypeId()
                        );

        assertTrue(result.isPresent());
        assertEquals(
                saved.getAllocationId(),
                result.get().getAllocationId()
        );
    }

    @Test
    void existsByShipmentIdAndContainerTypeId_shouldReturnTrue() {
        allocationRepository.saveAndFlush(
                createAllocation(4001L, containerType, 4)
        );

        boolean exists =
                allocationRepository
                        .existsByShipmentIdAndContainerTypeContainerTypeId(
                                4001L,
                                containerType.getContainerTypeId()
                        );

        assertTrue(exists);
    }

    @Test
    void existsByShipmentIdAndContainerTypeId_whenMissing_shouldReturnFalse() {
        boolean exists =
                allocationRepository
                        .existsByShipmentIdAndContainerTypeContainerTypeId(
                                999999L,
                                containerType.getContainerTypeId()
                        );

        assertFalse(exists);
    }

    @Test
    void save_whenShipmentAndContainerTypeAreDuplicated_shouldThrowException() {
        allocationRepository.saveAndFlush(
                createAllocation(5001L, containerType, 1)
        );

        ShipmentContainerAllocation duplicate =
                createAllocation(5001L, containerType, 3);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> allocationRepository.saveAndFlush(duplicate)
        );
    }

    @Test
    void save_whenQuantityIsZero_shouldThrowException() {
        ShipmentContainerAllocation invalidAllocation =
                createAllocation(6001L, containerType, 0);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> allocationRepository.saveAndFlush(invalidAllocation)
        );
    }

    @Test
    void delete_shouldRemoveAllocation() {
        ShipmentContainerAllocation saved =
                allocationRepository.saveAndFlush(
                        createAllocation(7001L, containerType, 1)
                );

        Long allocationId = saved.getAllocationId();

        allocationRepository.delete(saved);
        allocationRepository.flush();

        assertFalse(
                allocationRepository.findById(allocationId).isPresent()
        );
    }

    private ContainerType createContainerType(String typeCode) {
        return ContainerType.builder()
                .typeCode(typeCode)
                .typeName("Allocation Repository Test Container")
                .description("Used for allocation repository tests")
                .maxWeightKg(new BigDecimal("30000.00"))
                .maxVolumeCbm(new BigDecimal("67.70"))
                .lengthM(new BigDecimal("12.19"))
                .widthM(new BigDecimal("2.44"))
                .heightM(new BigDecimal("2.59"))
                .active(true)
                .build();
    }

    private ShipmentContainerAllocation createAllocation(
            Long shipmentId,
            ContainerType type,
            Integer quantity
    ) {
        return ShipmentContainerAllocation.builder()
                .shipmentId(shipmentId)
                .containerType(type)
                .quantity(quantity)
                .allocationStatus("ALLOCATED")
                .notes("Repository integration test")
                .build();
    }

    private String shortId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}