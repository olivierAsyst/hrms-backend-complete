-- Database initialization script for HRMS Backend
-- PostgreSQL version

-- Create database
CREATE DATABASE IF NOT EXISTS hrms_db;

-- Connect to database
\c hrms_db

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search

-- Create custom types
DO $$ BEGIN
    CREATE TYPE employee_status AS ENUM ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED', 'SUSPENDED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Function to update search vector for employees
CREATE OR REPLACE FUNCTION employee_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.first_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.middle_name, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.last_name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.email, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.job_title, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Initial data for permissions
INSERT INTO permissions (name, description, created_at, updated_at, created_by, updated_by, deleted, version) VALUES
('EMPLOYEE_READ', 'Read employee information', NOW(), NOW(), 'system', 'system', false, 0),
('EMPLOYEE_CREATE', 'Create new employees', NOW(), NOW(), 'system', 'system', false, 0),
('EMPLOYEE_UPDATE', 'Update employee information', NOW(), NOW(), 'system', 'system', false, 0),
('EMPLOYEE_DELETE', 'Delete employees', NOW(), NOW(), 'system', 'system', false, 0),
('DEPARTMENT_READ', 'Read department information', NOW(), NOW(), 'system', 'system', false, 0),
('DEPARTMENT_CREATE', 'Create new departments', NOW(), NOW(), 'system', 'system', false, 0),
('DEPARTMENT_UPDATE', 'Update department information', NOW(), NOW(), 'system', 'system', false, 0),
('DEPARTMENT_DELETE', 'Delete departments', NOW(), NOW(), 'system', 'system', false, 0),
('USER_READ', 'Read user information', NOW(), NOW(), 'system', 'system', false, 0),
('USER_CREATE', 'Create new users', NOW(), NOW(), 'system', 'system', false, 0),
('USER_UPDATE', 'Update user information', NOW(), NOW(), 'system', 'system', false, 0),
('USER_DELETE', 'Delete users', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_READ', 'Read role information', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_CREATE', 'Create new roles', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_UPDATE', 'Update role information', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_DELETE', 'Delete roles', NOW(), NOW(), 'system', 'system', false, 0),
('AUDIT_READ', 'Read audit logs', NOW(), NOW(), 'system', 'system', false, 0),
('SYSTEM_SETTINGS', 'Manage system settings', NOW(), NOW(), 'system', 'system', false, 0)
ON CONFLICT DO NOTHING;

-- Initial data for roles
INSERT INTO roles (name, description, created_at, updated_at, created_by, updated_by, deleted, version) VALUES
('ROLE_SUPER_ADMIN', 'Super Administrator with all permissions', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_ADMIN', 'Administrator', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_HR_MANAGER', 'HR Manager', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_MANAGER', 'Department Manager', NOW(), NOW(), 'system', 'system', false, 0),
('ROLE_EMPLOYEE', 'Regular Employee', NOW(), NOW(), 'system', 'system', false, 0)
ON CONFLICT DO NOTHING;

-- Assign permissions to roles
-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ADMIN gets most permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name IN ('EMPLOYEE_READ', 'EMPLOYEE_CREATE', 'EMPLOYEE_UPDATE', 'EMPLOYEE_DELETE',
               'DEPARTMENT_READ', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_DELETE',
               'USER_READ', 'USER_CREATE', 'USER_UPDATE', 'AUDIT_READ')
ON CONFLICT DO NOTHING;

-- HR_MANAGER gets employee and department management
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_HR_MANAGER'
AND p.name IN ('EMPLOYEE_READ', 'EMPLOYEE_CREATE', 'EMPLOYEE_UPDATE', 'EMPLOYEE_DELETE',
               'DEPARTMENT_READ', 'AUDIT_READ')
ON CONFLICT DO NOTHING;

-- MANAGER gets read access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER'
AND p.name IN ('EMPLOYEE_READ', 'DEPARTMENT_READ')
ON CONFLICT DO NOTHING;

-- EMPLOYEE gets basic read access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE'
AND p.name IN ('EMPLOYEE_READ')
ON CONFLICT DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_employees_search_vector ON employees USING gin(search_vector);
CREATE INDEX IF NOT EXISTS idx_employees_full_name ON employees(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_employees_status ON employees(status) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id) WHERE deleted = false;
CREATE INDEX IF NOT EXISTS idx_employees_hire_date ON employees(hire_date);

-- Create trigger for search vector update
DROP TRIGGER IF EXISTS employees_search_vector_trigger ON employees;
CREATE TRIGGER employees_search_vector_trigger
    BEFORE INSERT OR UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION employee_search_vector_update();

COMMENT ON TABLE employees IS 'Stores employee information with JSONB fields for flexibility';
COMMENT ON TABLE departments IS 'Stores department information';
COMMENT ON TABLE users IS 'Stores user authentication and authorization information';
COMMENT ON TABLE roles IS 'Stores role definitions';
COMMENT ON TABLE permissions IS 'Stores permission definitions';
COMMENT ON TABLE audit_logs IS 'Stores audit trail for all important actions';

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
