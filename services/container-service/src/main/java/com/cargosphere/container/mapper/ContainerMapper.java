package com.cargosphere.container.mapper;

import com.cargosphere.container.dto.AllocationResponse;
import com.cargosphere.container.dto.ContainerTypeResponse;
import com.cargosphere.container.entity.ContainerType;
import com.cargosphere.container.entity.ShipmentContainerAllocation;
import org.springframework.stereotype.Component;

@Component
public class ContainerMapper {

    public ContainerTypeResponse toContainerTypeResponse(ContainerType entity) {
        return new ContainerTypeResponse(
                entity.getContainerTypeId(),
                entity.getTypeCode(),
                entity.getTypeName(),
                entity.getDescription(),
                entity.getMaxWeightKg(),
                entity.getMaxVolumeCbm(),
                entity.getLengthM(),
                entity.getWidthM(),
                entity.getHeightM(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AllocationResponse toAllocationResponse(
            ShipmentContainerAllocation entity
    ) {
        return new AllocationResponse(
                entity.getAllocationId(),
                entity.getShipmentId(),
                entity.getContainerType().getContainerTypeId(),
                entity.getContainerType().getTypeCode(),
                entity.getContainerType().getTypeName(),
                entity.getQuantity(),
                entity.getAllocationStatus(),
                entity.getNotes(),
                entity.getAllocatedAt(),
                entity.getUpdatedAt()
        );
    }
}