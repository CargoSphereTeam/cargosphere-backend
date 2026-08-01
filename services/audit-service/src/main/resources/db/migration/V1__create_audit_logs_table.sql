CREATE SCHEMA IF NOT EXISTS audit_schema;

CREATE TABLE audit_schema.audit_logs
(
    id              BIGSERIAL PRIMARY KEY,

    actor_user_id   BIGINT,
    actor_role      VARCHAR(50),

    action           VARCHAR(80)  NOT NULL,
    entity_type      VARCHAR(50)  NOT NULL,
    entity_id        VARCHAR(100),

    service_name     VARCHAR(100) NOT NULL,
    description      VARCHAR(500) NOT NULL,
    outcome          VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',

    request_id       VARCHAR(100),
    ip_address       VARCHAR(45),
    http_method      VARCHAR(10),
    endpoint         VARCHAR(255),
    status_code      INTEGER,

    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_audit_outcome
        CHECK (outcome IN ('SUCCESS', 'FAILURE')),

    CONSTRAINT chk_audit_status_code
        CHECK (
            status_code IS NULL
            OR status_code BETWEEN 100 AND 599
        )
);

CREATE INDEX idx_audit_logs_actor_user_id
    ON audit_schema.audit_logs (actor_user_id);

CREATE INDEX idx_audit_logs_action
    ON audit_schema.audit_logs (action);

CREATE INDEX idx_audit_logs_entity
    ON audit_schema.audit_logs (
        entity_type,
        entity_id
    );

CREATE INDEX idx_audit_logs_service_name
    ON audit_schema.audit_logs (service_name);

CREATE INDEX idx_audit_logs_request_id
    ON audit_schema.audit_logs (request_id);

CREATE INDEX idx_audit_logs_created_at
    ON audit_schema.audit_logs (created_at DESC);


/*
 * Audit logs are append-only.
 *
 * Existing audit records cannot be updated or deleted.
 */
CREATE OR REPLACE FUNCTION
audit_schema.prevent_audit_log_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION
        'audit_logs is append-only; % operations are not allowed',
        TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_audit_log_update
BEFORE UPDATE ON audit_schema.audit_logs
FOR EACH ROW
EXECUTE FUNCTION
audit_schema.prevent_audit_log_modification();

CREATE TRIGGER trg_prevent_audit_log_delete
BEFORE DELETE ON audit_schema.audit_logs
FOR EACH ROW
EXECUTE FUNCTION
audit_schema.prevent_audit_log_modification();