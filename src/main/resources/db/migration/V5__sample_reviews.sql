-- ================================================
-- V5__sample_reviews.sql
-- Creates reviews table (previously missing) and inserts sample reviews
-- for delivered orders.
-- ================================================

CREATE TABLE IF NOT EXISTS reviews (
                                       id BIGSERIAL PRIMARY KEY,
                                       order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vendor_id BIGINT NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_reviews_vendor ON reviews(vendor_id);

INSERT INTO reviews (order_id, user_id, vendor_id, rating, comment, created_at)
VALUES
    (2, 2, 2, 5, 'Amazing taste and quick delivery!', NOW() - INTERVAL '2 DAYS'),
    (5, 2, 1, 4, 'Good food, could be hotter.', NOW() - INTERVAL '5 DAYS'),
    (7, 1, 1, 5, 'Best food experience so far.', NOW() - INTERVAL '7 DAYS'),
    (10, 1, 2, 3, 'Average food, but fast delivery.', NOW() - INTERVAL '3 DAYS'),
    (13, 3, 1, 4, 'Delicious and fresh!', NOW() - INTERVAL '6 DAYS'),
    (20, 1, 1, 5, 'Excellent packaging.', NOW() - INTERVAL '5 DAYS'),
    (27, 2, 2, 2, 'Late delivery, food was cold.', NOW() - INTERVAL '7 DAYS'),
    (33, 3, 1, 5, 'Loved the flavors!', NOW() - INTERVAL '5 DAYS'),
    (41, 1, 1, 4, 'Will order again.', NOW() - INTERVAL '1 DAY');