package com.cargosphere.container.service.impl;

import com.cargosphere.container.dto.ContainerTypeRequest;
import com.cargosphere.container.dto.ContainerTypeResponse;
import com.cargosphere.container.entity.ContainerType;
import com.cargosphere.container.exception.DuplicateResourceException;
import com.cargosphere.container.exception.ResourceNotFoundException;
import com.cargosphere.container.mapper.ContainerMapper;
import com.cargosphere.container.repository.ContainerTypeRepository;
import com.cargosphere.container.service.ContainerTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContainerTypeServiceImpl implements ContainerTypeService {

    private final ContainerTypeRepository containerTypeRepository;
    private final ContainerMapper containerMapper;

    @Override
    public ContainerTypeResponse createContainerType(
            ContainerTypeRequest request
    ) {
        String normalizedTypeCode = request.typeCode()
                .trim()
                .toUpperCase();

        if (containerTypeRepository.existsByTypeCodeIgnoreCase(normalizedTypeCode)) {
            throw new DuplicateResourceException(
                    "Container type already exists with code: "
                            + normalizedTypeCode
            );
        }

        ContainerType containerType = ContainerType.builder()
                .typeCode(normalizedTypeCode)
                .typeName(request.typeName().trim())
                .description(trimToNull(request.description()))
                .maxWeightKg(request.maxWeightKg())
                .maxVolumeCbm(request.maxVolumeCbm())
                .lengthM(request.lengthM())
                .widthM(request.widthM())
                .heightM(request.heightM())
                .active(request.active() == null || request.active())
                .build();

        ContainerType savedContainerType =
                containerTypeRepository.save(containerType);

        return containerMapper.toContainerTypeResponse(savedContainerType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContainerTypeResponse> getAllContainerTypes() {
        return containerTypeRepository.findAll()
                .stream()
                .map(containerMapper::toContainerTypeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContainerTypeResponse getContainerTypeById(Long containerTypeId) {
        ContainerType containerType =
                findContainerTypeById(containerTypeId);

        return containerMapper.toContainerTypeResponse(containerType);
    }

    @Override
    public ContainerTypeResponse updateContainerType(
            Long containerTypeId,
            ContainerTypeRequest request
    ) {
        ContainerType containerType =
                findContainerTypeById(containerTypeId);

        String normalizedTypeCode = request.typeCode()
                .trim()
                .toUpperCase();

        containerTypeRepository
                .findByTypeCodeIgnoreCase(normalizedTypeCode)
                .filter(existing ->
                        !existing.getContainerTypeId().equals(containerTypeId)
                )
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Container type already exists with code: "
                                    + normalizedTypeCode
                    );
                });

        containerType.setTypeCode(normalizedTypeCode);
        containerType.setTypeName(request.typeName().trim());
        containerType.setDescription(trimToNull(request.description()));
        containerType.setMaxWeightKg(request.maxWeightKg());
        containerType.setMaxVolumeCbm(request.maxVolumeCbm());
        containerType.setLengthM(request.lengthM());
        containerType.setWidthM(request.widthM());
        containerType.setHeightM(request.heightM());

        if (request.active() != null) {
            containerType.setActive(request.active());
        }

        ContainerType updatedContainerType =
                containerTypeRepository.save(containerType);

        return containerMapper.toContainerTypeResponse(updatedContainerType);
    }

    @Override
    public void deleteContainerType(Long containerTypeId) {
        ContainerType containerType =
                findContainerTypeById(containerTypeId);

        containerTypeRepository.delete(containerType);
    }

    private ContainerType findContainerTypeById(Long containerTypeId) {
        return containerTypeRepository.findById(containerTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Container type not found with ID: "
                                        + containerTypeId
                        )
                );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}