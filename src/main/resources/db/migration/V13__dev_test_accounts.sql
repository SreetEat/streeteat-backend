-- ================================================
-- V13__dev_test_accounts.sql
--
-- Creates known, reusable test accounts with a REAL bcrypt-hashed password
-- (generated with Python's bcrypt library, rounds=10 -- Spring's
-- BCryptPasswordEncoder verifies $2a$/$2b$ hashes interchangeably, so this
-- works correctly against AuthController's login flow, unlike V2's earlier
-- sample users whose "password123" is stored as plain text and can never
-- actually be logged into).
--
-- Password for every account below: password123
--
-- WHY THIS MIGRATION EXISTS: every `docker compose down -v` wipes the
-- database completely, and up to this point that meant manually
-- re-registering a vendor, re-creating the stall, and re-adding menu items
-- from scratch after every single reset -- which is exactly what cost this
-- session so many rounds of "invalid email/password" and "vendor id 3
-- doesn't exist" confusion. This migration removes that step permanently:
-- a fresh `down -v && up --build` now always leaves behind a working
-- vendor and customer to log into immediately.
--
-- THIS IS A DEV/TEST CONVENIENCE, NOT SOMETHING FOR PRODUCTION: shipping a
-- known-password seeded account to a real deployment would be a real
-- security hole. Before any real deployment, this migration should either
-- be deleted or gated behind the same kind of "only in dev/test profile"
-- check the rest of this project already uses for other test-only wiring.
-- ================================================

INSERT INTO users (name, email, role, password, phone, is_active, created_at, updated_at)
VALUES ('Test Vendor', 'vendor@test.com', 'VENDOR',
        '$2b$10$hsNOyhX.mBV92Zravs0JreDU9rxXcvjYNXRPDzkzGgyk2R273D9ai',
        '9998887777', true, now(), now())
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, role, password, phone, is_active, created_at, updated_at)
VALUES ('Test Customer', 'customer@test.com', 'CUSTOMER',
        '$2b$10$hsNOyhX.mBV92Zravs0JreDU9rxXcvjYNXRPDzkzGgyk2R273D9ai',
        '9998887778', true, now(), now())
    ON CONFLICT (email) DO NOTHING;

-- Vendor's stall, already fully set up -- no /setup step needed.
INSERT INTO vendors (name, address, phone, email, owner_user_id, latitude, longitude, is_active, created_at, updated_at)
SELECT 'Test Vendor Stall', 'Sector 62, Noida', '9998887777', 'vendor@test.com',
       u.id, 28.6139, 77.2090, true, now(), now()
FROM users u
WHERE u.email = 'vendor@test.com'
    ON CONFLICT (email) DO NOTHING;

-- A couple of menu items so the customer app has something real to order.
INSERT INTO menu_items (vendor_id, name, price, available)
SELECT v.id, 'Test Samosa', 20.00, true FROM vendors v WHERE v.email = 'vendor@test.com';

INSERT INTO menu_items (vendor_id, name, price, available)
SELECT v.id, 'Test Chai', 15.00, true FROM vendors v WHERE v.email = 'vendor@test.com'\