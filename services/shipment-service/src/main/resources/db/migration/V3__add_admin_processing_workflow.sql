ALTER TABLE shipment_schema.shipments
    ADD COLUMN processing_stage VARCHAR(40)
        NOT NULL DEFAULT 'PENDING_ADMIN_REVIEW',

    ADD COLUMN processing_started_at TIMESTAMP,

    ADD COLUMN processing_completed_at TIMESTAMP,

    ADD COLUMN ebill_number VARCHAR(50),

    ADD COLUMN ebill_version INTEGER,

    ADD COLUMN ebill_generated_at TIMESTAMP,

    ADD COLUMN ebill_generated_by BIGINT,

    ADD COLUMN ebill_snapshot JSONB;


ALTER TABLE shipment_schema.shipments
    ADD CONSTRAINT chk_shipments_processing_stage
        CHECK (
            processing_stage IN (
                                 'PENDING_ADMIN_REVIEW',
                                 'CONTAINER_ALLOCATION',
                                 'CARGO_VERIFICATION',
                                 'DOCUMENT_VERIFICATION',
                                 'PAYMENT_CONFIRMATION',
                                 'READY_FOR_EBILL',
                                 'EBILL_GENERATED'
                )
            ),

    ADD CONSTRAINT chk_shipments_processing_dates
        CHECK (
            processing_completed_at IS NULL
            OR (
                processing_started_at IS NOT NULL
                AND processing_completed_at >= processing_started_at
            )
        ),

    ADD CONSTRAINT chk_shipments_ebill_version
        CHECK (
            ebill_version IS NULL
            OR ebill_version > 0
        ),

    ADD CONSTRAINT uk_shipments_ebill_number
        UNIQUE (ebill_number),

    ADD CONSTRAINT chk_shipments_ebill_fields_consistent
        CHECK (
            (
                ebill_number IS NULL
                AND ebill_version IS NULL
                AND ebill_generated_at IS NULL
                AND ebill_generated_by IS NULL
                AND ebill_snapshot IS NULL
            )
            OR
            (
                ebill_number IS NOT NULL
                AND ebill_version IS NOT NULL
                AND ebill_generated_at IS NOT NULL
                AND ebill_generated_by IS NOT NULL
                AND ebill_snapshot IS NOT NULL
            )
        ),

    ADD CONSTRAINT chk_shipments_generated_ebill_stage
        CHECK (
            processing_stage <> 'EBILL_GENERATED'
            OR (
                ebill_number IS NOT NULL
                AND ebill_version IS NOT NULL
                AND ebill_generated_at IS NOT NULL
                AND ebill_generated_by IS NOT NULL
                AND ebill_snapshot IS NOT NULL
                AND processing_completed_at IS NOT NULL
            )
        );


CREATE INDEX idx_shipments_processing_stage
    ON shipment_schema.shipments(processing_stage);


CREATE TABLE shipment_schema.cargo_verifications (
                                                     id BIGSERIAL PRIMARY KEY,

                                                     cargo_detail_id BIGINT NOT NULL,

                                                     verification_status VARCHAR(20)
                                                                            NOT NULL DEFAULT 'DRAFT',

                                                     confirmed_cargo_name VARCHAR(100),

                                                     confirmed_cargo_description VARCHAR(255),

                                                     confirmed_cargo_type VARCHAR(50),

                                                     confirmed_weight_kg NUMERIC(13, 3),

                                                     confirmed_volume_cbm NUMERIC(13, 3),

                                                     confirmed_quantity INTEGER,

                                                     confirmed_is_fragile BOOLEAN,

                                                     confirmed_is_hazardous BOOLEAN,

                                                     verification_remarks VARCHAR(500),

                                                     verified_by BIGINT,

                                                     verified_at TIMESTAMP,

                                                     created_at TIMESTAMP
                                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                     updated_at TIMESTAMP
                                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                     CONSTRAINT fk_cargo_verifications_cargo_detail
                                                         FOREIGN KEY (cargo_detail_id)
                                                             REFERENCES shipment_schema.cargo_details(id)
                                                             ON DELETE CASCADE,

                                                     CONSTRAINT uk_cargo_verifications_cargo_detail
                                                         UNIQUE (cargo_detail_id),

                                                     CONSTRAINT chk_cargo_verifications_status
                                                         CHECK (
                                                             verification_status IN (
                                                                                     'DRAFT',
                                                                                     'CONFIRMED'
                                                                 )
                                                             ),

                                                     CONSTRAINT chk_cargo_verifications_type
                                                         CHECK (
                                                             confirmed_cargo_type IS NULL
                                                                 OR confirmed_cargo_type IN (
                                                                                             'GENERAL',
                                                                                             'FRAGILE',
                                                                                             'HAZARDOUS',
                                                                                             'PERISHABLE',
                                                                                             'LIQUID',
                                                                                             'HEAVY',
                                                                                             'ELECTRONICS',
                                                                                             'OTHER'
                                                                 )
                                                             ),

                                                     CONSTRAINT chk_cargo_verifications_weight
                                                         CHECK (
                                                             confirmed_weight_kg IS NULL
                                                                 OR confirmed_weight_kg > 0
                                                             ),

                                                     CONSTRAINT chk_cargo_verifications_volume
                                                         CHECK (
                                                             confirmed_volume_cbm IS NULL
                                                                 OR confirmed_volume_cbm > 0
                                                             ),

                                                     CONSTRAINT chk_cargo_verifications_quantity
                                                         CHECK (
                                                             confirmed_quantity IS NULL
                                                                 OR confirmed_quantity > 0
                                                             ),

                                                     CONSTRAINT chk_cargo_verifications_confirmed_values
                                                         CHECK (
                                                             verification_status <> 'CONFIRMED'
                                                                 OR (
                                                                 confirmed_cargo_name IS NOT NULL
                                                                     AND confirmed_weight_kg IS NOT NULL
                                                                     AND confirmed_quantity IS NOT NULL
                                                                     AND confirmed_is_fragile IS NOT NULL
                                                                     AND confirmed_is_hazardous IS NOT NULL
                                                                     AND verified_by IS NOT NULL
                                                                     AND verified_at IS NOT NULL
                                                                 )
                                                             )
);


CREATE INDEX idx_cargo_verifications_status
    ON shipment_schema.cargo_verifications(verification_status);


ALTER TABLE shipment_schema.shipment_events
DROP CONSTRAINT IF EXISTS chk_shipment_events_event_type;


ALTER TABLE shipment_schema.shipment_events
    ADD CONSTRAINT chk_shipment_events_event_type
        CHECK (
            event_type IN (
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
                           'CONTAINER_ALLOCATED',
                           'ADMIN_PROCESSING_STARTED',
                           'CARGO_VERIFIED',
                           'DOCUMENTS_VERIFIED',
                           'PAYMENT_CONFIRMED',
                           'EBILL_GENERATED'
                )
            );


CREATE TRIGGER trg_cargo_verifications_set_updated_at
    BEFORE UPDATE ON shipment_schema.cargo_verifications
    FOR EACH ROW
    EXECUTE FUNCTION shipment_schema.set_updated_at();