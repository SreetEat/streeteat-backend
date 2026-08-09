-- ================================================
-- V11__delivery_partner_owner.sql
-- Links a delivery_partners row to the User account (role = DELIVERY_PARTNER)
-- that owns it, mirroring the vendors.owner_user_id pattern from V10. This
-- enables self-service update/delete: a partner can manage their own
-- profile without needing ADMIN.
-- ================================================

ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS owner_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL;

-- DeliveryPartner.area was mapped by the entity but no migration had ever
-- created this column (V1 only created name/phone/vehicle_number) -- would
-- fail under ddl-auto=validate.
ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS area VARCHAR(100);

-- Same issue as area above: DeliveryPartner.earnings is a plain double field
-- with no @Column override, so Hibernate expects a NOT NULL numeric
-- "earnings" column. Nothing had ever created it -- confirmed by an actual
-- docker-compose run against real Postgres, which failed schema validation
-- with "missing column [earnings] in table [delivery_partners]".
ALTER TABLE delivery_partners
    ADD COLUMN IF NOT EXISTS earnings DOUBLE PRECISION NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_delivery_partners_owner ON delivery_partners(owner_user_id);