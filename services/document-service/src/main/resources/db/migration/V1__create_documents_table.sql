CREATE SCHEMA IF NOT EXISTS document_schema;

CREATE TABLE document_schema.documents (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_by BIGINT,
    verified_at TIMESTAMP,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_documents_shipment_type
        UNIQUE (shipment_id, document_type),

    CONSTRAINT chk_documents_verification_status
        CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

CREATE INDEX idx_documents_shipment_id
    ON document_schema.documents(shipment_id);

CREATE INDEX idx_documents_verification_status
    ON document_schema.documents(verification_status);

CREATE OR REPLACE FUNCTION document_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_documents_set_updated_at
    BEFORE UPDATE ON document_schema.documents
    FOR EACH ROW
    EXECUTE FUNCTION document_schema.set_updated_at();