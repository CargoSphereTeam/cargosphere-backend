package com.cargosphere.container.repository;

import com.cargosphere.container.entity.ContainerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContainerTypeRepository extends JpaRepository<ContainerType, Long> {

    Optional<ContainerType> findByTypeCodeIgnoreCase(String typeCode);

    boolean existsByTypeCodeIgnoreCase(String typeCode);
}