-- Rename ROLE_CUSTOMER to ROLE_CLIENT
UPDATE auth_schema.roles
SET name = 'ROLE_CLIENT',
    description = 'Client user who creates and tracks shipments',
    updated_at = CURRENT_TIMESTAMP
WHERE name = 'ROLE_CUSTOMER';

-- Insert ROLE_CLIENT if it does not already exist
INSERT INTO auth_schema.roles (name, description)
SELECT 'ROLE_CLIENT', 'Client user who creates and tracks shipments'
WHERE NOT EXISTS (
    SELECT 1 FROM auth_schema.roles WHERE name = 'ROLE_CLIENT'
);

-- If any user has ROLE_STAFF, move them to ROLE_CLIENT
UPDATE auth_schema.users
SET role_id = (
    SELECT id FROM auth_schema.roles WHERE name = 'ROLE_CLIENT'
)
WHERE role_id = (
    SELECT id FROM auth_schema.roles WHERE name = 'ROLE_STAFF'
);

-- Remove ROLE_STAFF
DELETE FROM auth_schema.roles
WHERE name = 'ROLE_STAFF';