package com.cargosphere.container.repository;

import com.cargosphere.container.entity.ContainerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ContainerTypeRepositoryTest {

    @Autowired
    private ContainerTypeRepository containerTypeRepository;

    @Test
    void save_shouldPersistContainerType() {
        ContainerType containerType = createContainerType("TEST-" + shortId());

        ContainerType saved = containerTypeRepository.saveAndFlush(containerType);

        assertNotNull(saved.getContainerTypeId());
        assertEquals(containerType.getTypeCode(), saved.getTypeCode());
        assertTrue(saved.getActive());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void findByTypeCodeIgnoreCase_shouldFindExistingType() {
        String typeCode = "CASE-" + shortId();

        ContainerType saved = containerTypeRepository.saveAndFlush(
                createContainerType(typeCode)
        );

        Optional<ContainerType> result =
                containerTypeRepository.findByTypeCodeIgnoreCase(
                        typeCode.toLowerCase()
                );

        assertTrue(result.isPresent());
        assertEquals(
                saved.getContainerTypeId(),
                result.get().getContainerTypeId()
        );
        assertEquals(typeCode, result.get().getTypeCode());
    }

    @Test
    void existsByTypeCodeIgnoreCase_shouldReturnTrue() {
        String typeCode = "EXISTS-" + shortId();

        containerTypeRepository.saveAndFlush(
                createContainerType(typeCode)
        );

        boolean exists =
                containerTypeRepository.existsByTypeCodeIgnoreCase(
                        typeCode.toLowerCase()
                );

        assertTrue(exists);
    }

    @Test
    void existsByTypeCodeIgnoreCase_whenMissing_shouldReturnFalse() {
        boolean exists =
                containerTypeRepository.existsByTypeCodeIgnoreCase(
                        "MISSING-" + UUID.randomUUID()
                );

        assertFalse(exists);
    }

    @Test
    void save_whenTypeCodeIsDuplicate_shouldThrowException() {
        String typeCode = "DUP-" + shortId();

        containerTypeRepository.saveAndFlush(
                createContainerType(typeCode)
        );

        ContainerType duplicate = createContainerType(typeCode);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> containerTypeRepository.saveAndFlush(duplicate)
        );
    }

    @Test
    void delete_shouldRemoveContainerType() {
        ContainerType saved = containerTypeRepository.saveAndFlush(
                createContainerType("DELETE-" + shortId())
        );

        Long id = saved.getContainerTypeId();

        containerTypeRepository.delete(saved);
        containerTypeRepository.flush();

        assertFalse(containerTypeRepository.findById(id).isPresent());
    }

    private ContainerType createContainerType(String typeCode) {
        return ContainerType.builder()
                .typeCode(typeCode)
                .typeName("Repository Test Container")
                .description("Created by repository integration test")
                .maxWeightKg(new BigDecimal("28000.00"))
                .maxVolumeCbm(new BigDecimal("33.20"))
                .lengthM(new BigDecimal("6.06"))
                .widthM(new BigDecimal("2.44"))
                .heightM(new BigDecimal("2.59"))
                .active(true)
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