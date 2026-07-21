package com.cargosphere.container.service;

import com.cargosphere.container.dto.ContainerTypeRequest;
import com.cargosphere.container.dto.ContainerTypeResponse;

import java.util.List;

public interface ContainerTypeService {

    ContainerTypeResponse createContainerType(ContainerTypeRequest request);

    List<ContainerTypeResponse> getAllContainerTypes();

    ContainerTypeResponse getContainerTypeById(Long containerTypeId);

    ContainerTypeResponse updateContainerType(
            Long containerTypeId,
            ContainerTypeRequest request
    );

    void deleteContainerType(Long containerTypeId);
}