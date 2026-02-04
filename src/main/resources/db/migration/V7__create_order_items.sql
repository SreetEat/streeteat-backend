-- ================================================
-- V7__create_order_items.sql
-- Creates order_items table
-- ================================================

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             menu_item_id BIGINT NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
                             quantity INT NOT NULL,
                             price NUMERIC(10,2) NOT NULL
);
