-- V9__indexes_constraints.sql
-- Production-grade constraints, indexes & soft delete support

-- ======================================
-- USERS TABLE
-- ======================================
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- User.phone is mapped by the entity (@Column(length = 20)) but no
-- migration had ever created this column -- would fail under
-- ddl-auto=validate. Confirmed by an actual docker-compose run against
-- real Postgres: "Schema-validation: missing column [phone] in table [users]".
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Index for faster lookups by role
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ======================================
-- VENDORS TABLE
-- ======================================
ALTER TABLE vendors
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

ALTER TABLE vendors
    ADD CONSTRAINT uq_vendors_name UNIQUE (name);

-- Composite index (name + phone) for vendor search
CREATE INDEX IF NOT EXISTS idx_vendors_name_phone ON vendors(name, phone);

-- ======================================
-- DELIVERY PARTNERS TABLE
-- ======================================
ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- These columns were referenced below (unique constraint on email, index on
-- status) but never actually created by V1, which would fail this migration
-- on a fresh database. Also added lat/lng for Haversine-based partner assignment.
ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS email VARCHAR(140);

ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;

ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_delivery_partners_email'
    ) THEN
ALTER TABLE delivery_partners
    ADD CONSTRAINT uq_delivery_partners_email UNIQUE (email);
END IF;
END $$;

-- Index for availability/status queries
CREATE INDEX IF NOT EXISTS idx_delivery_status ON delivery_partners(status);

-- ======================================
-- ORDERS TABLE
-- ======================================
-- NOTE: FK constraints for orders (user_id, vendor_id, delivery_partner_id)
-- are already declared inline in V1's CREATE TABLE statement. The versions
-- previously added here were redundant, and fk_orders_vendor additionally
-- used "ON DELETE SET NULL" against vendor_id, which V1 declares NOT NULL --
-- contradictory, and would fail the moment a vendor was ever deleted.
--
-- The uq_orders_user_vendor_time unique constraint that used to be here was
-- also removed: customers legitimately place more than one order to the same
-- vendor, so it wasn't a sound business rule, and it was also technically
-- broken -- V3's sample data has rows sharing an identical NOW()-derived
-- timestamp for the same (user_id, vendor_id) pair (NOW() is constant across
-- all rows in a single INSERT statement in Postgres), so this constraint
-- would have failed against the very seed data in this migration set.

-- Composite index for order lookups
CREATE INDEX IF NOT EXISTS idx_orders_vendor_status ON orders(vendor_id, status);

-- ======================================
-- GENERAL INDEXING
-- ======================================
-- Orders created_at indexing for analytics
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);