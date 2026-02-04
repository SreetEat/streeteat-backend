-- V9__indexes_constraints.sql
-- Production-grade constraints, indexes & soft delete support

-- ======================================
-- USERS TABLE
-- ======================================
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

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

ALTER TABLE delivery_partners
    ADD CONSTRAINT uq_delivery_partners_email UNIQUE (email);

-- Index for availability/status queries
CREATE INDEX IF NOT EXISTS idx_delivery_status ON delivery_partners(status);

-- ======================================
-- ORDERS TABLE
-- ======================================
-- Foreign key to users (customer)
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT;

-- Foreign key to vendors
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_vendor FOREIGN KEY (vendor_id)
        REFERENCES vendors(id)
        ON DELETE SET NULL;

-- Foreign key to delivery partners
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_delivery FOREIGN KEY (delivery_partner_id)
        REFERENCES delivery_partners(id)
        ON DELETE SET NULL;

-- Composite index for order lookups
CREATE INDEX IF NOT EXISTS idx_orders_vendor_status ON orders(vendor_id, status);

-- ======================================
-- GENERAL INDEXING
-- ======================================
-- Orders created_at indexing for analytics
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

-- Ensure no duplicate orders (user + vendor + created_at)
ALTER TABLE orders
    ADD CONSTRAINT uq_orders_user_vendor_time UNIQUE (user_id, vendor_id, created_at);
