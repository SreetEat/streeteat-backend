-- ================================================
-- V2__sample_data.sql
-- Inserts sample data for testing
-- ================================================

-- Insert sample users
INSERT INTO users (name, email, role, password)
VALUES
    ('Alice Johnson', 'alice@example.com', 'CUSTOMER', 'password123'),
    ('Bob Vendor', 'bob@example.com', 'VENDOR', 'password123'),
    ('Charlie Rider', 'charlie@example.com', 'DELIVERY_PARTNER', 'password123'),
    ('Admin User', 'admin@example.com', 'ADMIN', 'adminpass');

-- Insert sample vendors
-- NOTE: vendors.email is NOT NULL + UNIQUE per V1__init.sql. This insert
-- previously omitted it entirely, which only fails once Flyway actually
-- applies this script against a real (empty) Postgres database -- it was
-- invisible under the H2 test profile because Flyway is disabled there and
-- Hibernate's ddl-auto=update never runs this file at all.
INSERT INTO vendors (name, address, phone, email)
VALUES
    ('Tasty Bites', '123 Main Street, Cityville', '9876543210', 'tastybites@streeteat.example.com'),
    ('Street Treats', '45 Market Road, Townsville', '9123456780', 'streettreats@streeteat.example.com');

-- Insert sample delivery partners
INSERT INTO delivery_partners (name, phone, vehicle_number)
VALUES
    ('Charlie Rider', '9998887777', 'DL01AB1234'),
    ('David Dasher', '8887776666', 'MH12XY7890');

-- Insert sample orders
INSERT INTO orders (user_id, vendor_id, delivery_partner_id, status, created_at, updated_at)
VALUES
    (1, 1, 1, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1, 2, 2, 'ASSIGNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, NULL, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 2, 1, 'DELIVERED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);