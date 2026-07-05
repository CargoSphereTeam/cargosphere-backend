CREATE SCHEMA IF NOT EXISTS auth_schema;

CREATE TABLE auth_schema.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_schema.users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES auth_schema.roles(id),

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE INDEX idx_users_role_id ON auth_schema.users(role_id);

CREATE OR REPLACE FUNCTION auth_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_roles_set_updated_at
BEFORE UPDATE ON auth_schema.roles
FOR EACH ROW
EXECUTE FUNCTION auth_schema.set_updated_at();

CREATE TRIGGER trg_users_set_updated_at
BEFORE UPDATE ON auth_schema.users
FOR EACH ROW
EXECUTE FUNCTION auth_schema.set_updated_at();

INSERT INTO auth_schema.roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator with full system access'),
('ROLE_STAFF', 'Staff user who manages shipments and operations'),
('ROLE_CUSTOMER', 'Customer user who creates and tracks shipments');