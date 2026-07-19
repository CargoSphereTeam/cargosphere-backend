CREATE SCHEMA IF NOT EXISTS shipment_schema;

CREATE TABLE shipment_schema.shipments (
                                           id BIGSERIAL PRIMARY KEY,
                                           shipment_number VARCHAR(50) NOT NULL UNIQUE,
                                           client_user_id BIGINT NOT NULL,
                                           origin_location VARCHAR(150) NOT NULL,
                                           destination_location VARCHAR(150) NOT NULL,
                                           shipment_type VARCHAR(50) NOT NULL,
                                           status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
                                           expected_pickup_date DATE,
                                           expected_delivery_date DATE,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           CONSTRAINT chk_shipments_status
                                               CHECK (status IN (
                                                                 'CREATED',
                                                                 'BOOKED',
                                                                 'IN_TRANSIT',
                                                                 'DELIVERED',
                                                                 'CANCELLED'
                                                   )),

                                           CONSTRAINT chk_shipments_type
                                               CHECK (shipment_type IN (
                                                                        'ROAD',
                                                                        'RAIL',
                                                                        'SEA',
                                                                        'AIR'
                                                   ))
);

CREATE TABLE shipment_schema.cargo_details (
                                               id BIGSERIAL PRIMARY KEY,
                                               shipment_id BIGINT NOT NULL,
                                               cargo_name VARCHAR(100) NOT NULL,
                                               cargo_description VARCHAR(255),
                                               cargo_type VARCHAR(50),
                                               weight_kg NUMERIC(12, 2) NOT NULL,
                                               volume_cbm NUMERIC(12, 2),
                                               quantity INTEGER NOT NULL DEFAULT 1,
                                               is_fragile BOOLEAN NOT NULL DEFAULT FALSE,
                                               is_hazardous BOOLEAN NOT NULL DEFAULT FALSE,
                                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                               CONSTRAINT fk_cargo_details_shipment
                                                   FOREIGN KEY (shipment_id)
                                                       REFERENCES shipment_schema.shipments(id)
                                                       ON DELETE CASCADE,

                                               CONSTRAINT chk_cargo_weight_positive
                                                   CHECK (weight_kg > 0),

                                               CONSTRAINT chk_cargo_volume_positive
                                                   CHECK (volume_cbm IS NULL OR volume_cbm > 0),

                                               CONSTRAINT chk_cargo_quantity_positive
                                                   CHECK (quantity > 0),

                                               CONSTRAINT chk_cargo_type
                                                   CHECK (
                                                       cargo_type IS NULL OR cargo_type IN (
                                                                                            'GENERAL',
                                                                                            'FRAGILE',
                                                                                            'HAZARDOUS',
                                                                                            'PERISHABLE',
                                                                                            'LIQUID',
                                                                                            'HEAVY',
                                                                                            'ELECTRONICS',
                                                                                            'OTHER'
                                                           )
                                                       )
);

CREATE TABLE shipment_schema.shipment_events (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 shipment_id BIGINT NOT NULL,
                                                 event_type VARCHAR(50) NOT NULL,
                                                 event_description VARCHAR(255),
                                                 event_location VARCHAR(150),
                                                 event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                 CONSTRAINT fk_shipment_events_shipment
                                                     FOREIGN KEY (shipment_id)
                                                         REFERENCES shipment_schema.shipments(id)
                                                         ON DELETE CASCADE,

                                                 CONSTRAINT chk_shipment_events_event_type
                                                     CHECK (event_type IN (
                                                                           'CREATED',
                                                                           'CARGO_ADDED',
                                                                           'BOOKED',
                                                                           'PICKED_UP',
                                                                           'DEPARTED',
                                                                           'IN_TRANSIT',
                                                                           'ARRIVED',
                                                                           'OUT_FOR_DELIVERY',
                                                                           'DELIVERED',
                                                                           'CANCELLED',
                                                                           'DELAYED',
                                                                           'CONTAINER_ALLOCATED'
                                                         ))
);

CREATE INDEX idx_shipments_client_user_id
    ON shipment_schema.shipments(client_user_id);

CREATE INDEX idx_shipments_status
    ON shipment_schema.shipments(status);

CREATE INDEX idx_shipments_shipment_number
    ON shipment_schema.shipments(shipment_number);

CREATE INDEX idx_cargo_details_shipment_id
    ON shipment_schema.cargo_details(shipment_id);

CREATE INDEX idx_shipment_events_shipment_id
    ON shipment_schema.shipment_events(shipment_id);

CREATE INDEX idx_shipment_events_event_type
    ON shipment_schema.shipment_events(event_type);

CREATE OR REPLACE FUNCTION shipment_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shipments_set_updated_at
    BEFORE UPDATE ON shipment_schema.shipments
    FOR EACH ROW
    EXECUTE FUNCTION shipment_schema.set_updated_at();

CREATE TRIGGER trg_cargo_details_set_updated_at
    BEFORE UPDATE ON shipment_schema.cargo_details
    FOR EACH ROW
    EXECUTE FUNCTION shipment_schema.set_updated_at();

CREATE TRIGGER trg_shipment_events_set_updated_at
    BEFORE UPDATE ON shipment_schema.shipment_events
    FOR EACH ROW
    EXECUTE FUNCTION shipment_schema.set_updated_at();