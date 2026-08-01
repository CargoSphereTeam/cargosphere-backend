ALTER TABLE shipment_schema.cargo_details
ALTER COLUMN weight_kg
        TYPE NUMERIC(13, 3)
        USING weight_kg::NUMERIC(13, 3),

    ALTER COLUMN volume_cbm
        TYPE NUMERIC(13, 3)
        USING volume_cbm::NUMERIC(13, 3);