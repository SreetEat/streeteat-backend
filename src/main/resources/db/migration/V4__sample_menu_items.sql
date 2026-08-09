-- ================================================
-- V4__sample_menu_items.sql
-- Creates menu_items table (previously missing -- this file inserted into
-- it without ever creating it, which would fail on a fresh database) and
-- inserts sample menu items for vendors.
-- ================================================

CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_menu_items_vendor ON menu_items(vendor_id);

INSERT INTO menu_items (vendor_id, name, price, available) VALUES
-- Vendor 1: Spicy Bites
(1, 'Paneer Butter Masala', 180.00, true),
(1, 'Chicken Biryani', 220.00, true),
(1, 'Veg Momos', 100.00, true),
(1, 'Tandoori Chicken', 250.00, true),
(1, 'Masala Dosa', 90.00, true),

-- Vendor 2: Tandoori Treats
(2, 'Butter Naan', 40.00, true),
(2, 'Dal Makhani', 150.00, true),
(2, 'Chicken Tikka', 200.00, true),
(2, 'Veg Thali', 180.00, true),
(2, 'Gulab Jamun', 70.00, true);
