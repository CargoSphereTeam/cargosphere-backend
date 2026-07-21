CREATE SCHEMA IF NOT EXISTS container_schema;

CREATE TABLE container_schema.container_types (
    container_type_id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(30) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    max_weight_kg NUMERIC(12, 2) NOT NULL,
    max_volume_cbm NUMERIC(12, 2) NOT NULL,
    length_m NUMERIC(8, 2),
    width_m NUMERIC(8, 2),
    height_m NUMERIC(8, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_container_max_weight
        CHECK (max_weight_kg > 0),

    CONSTRAINT chk_container_max_volume
        CHECK (max_volume_cbm > 0)
);

CREATE TABLE container_schema.shipment_container_allocations (
    allocation_id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    container_type_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    allocation_status VARCHAR(30) NOT NULL DEFAULT 'ALLOCATED',
    notes VARCHAR(255),
    allocated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_allocation_container_type
        FOREIGN KEY (container_type_id)
        REFERENCES container_schema.container_types(container_type_id),

    CONSTRAINT chk_allocation_quantity
        CHECK (quantity > 0),

    CONSTRAINT uq_shipment_container_type
        UNIQUE (shipment_id, container_type_id)
);

CREATE INDEX idx_allocation_shipment_id
    ON container_schema.shipment_container_allocations(shipment_id);

CREATE INDEX idx_allocation_container_type_id
    ON container_schema.shipment_container_allocations(container_type_id);