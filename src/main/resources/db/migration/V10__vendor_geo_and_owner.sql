-- ================================================
-- V10__vendor_geo_and_owner.sql
-- Adds columns required for:
--  - Haversine-based "nearby vendors" search (vendors.latitude/longitude)
--  - Linking a vendor profile to the User account that owns/manages it
-- ================================================

ALTER TABLE vendors
    ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;

ALTER TABLE vendors
    ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

ALTER TABLE vendors
    ADD COLUMN IF NOT EXISTS owner_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_vendors_owner ON vendors(owner_user_id);

-- Backfill sample vendors with rough coordinates so /api/vendors/nearby has
-- something to return out of the box (Delhi-NCR area, arbitrary demo points).
UPDATE vendors SET latitude = 28.6139, longitude = 77.2090 WHERE name = 'Tasty Bites' AND latitude IS NULL;
UPDATE vendors SET latitude = 28.5355, longitude = 77.3910 WHERE name = 'Street Treats' AND latitude IS NULL;

-- The `deliveries` table (tracked by the Delivery entity: id, order_id,
-- partner_id, status) previously had no Flyway migration at all -- it only
-- existed because Hibernate ddl-auto=update silently created it. That's
-- fragile and breaks under ddl-auto=validate (used in the staging profile).
CREATE TABLE IF NOT EXISTS deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    partner_id BIGINT REFERENCES delivery_partners(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_deliveries_order ON deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_partner ON deliveries(partner_id);
