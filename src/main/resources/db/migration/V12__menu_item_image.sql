-- ================================================
-- V12__menu_item_image.sql
-- Adds optional dish photo support to menu items.
--
-- Stored as a TEXT column holding either a normal image URL or a base64
-- data URI. There is no object storage (S3 or similar) wired up in this
-- project yet, so the vendor frontend resizes/compresses the image
-- client-side and sends it as a base64 data URI, which lands here as plain
-- text. This is a reasonable MVP choice -- it works with zero extra
-- infrastructure -- but it is NOT how this should work at real scale:
-- storing images as text in Postgres bloats the table and doesn't belong
-- in a relational DB long-term. Before real production use, this should
-- move to actual object storage (S3, Cloudinary, etc.) with this column
-- holding just the resulting URL.
-- ================================================

ALTER TABLE menu_items
    ADD COLUMN IF NOT EXISTS image_url TEXT;