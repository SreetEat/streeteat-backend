-- ================================================
-- V6__sample_payments.sql
-- Creates payments table (previously missing) and inserts sample payments
-- for delivered orders.
-- ================================================

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    amount NUMERIC(10,2) NOT NULL,
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id);

INSERT INTO payments (order_id, amount, method, status, created_at)
VALUES
    (2, 220.00, 'UPI', 'SUCCESS', NOW() - INTERVAL '2 DAYS'),
    (5, 180.00, 'CARD', 'SUCCESS', NOW() - INTERVAL '5 DAYS'),
    (7, 250.00, 'CASH', 'SUCCESS', NOW() - INTERVAL '7 DAYS'),
    (10, 200.00, 'UPI', 'SUCCESS', NOW() - INTERVAL '3 DAYS'),
    (13, 180.00, 'CARD', 'SUCCESS', NOW() - INTERVAL '6 DAYS'),
    (20, 90.00, 'UPI', 'SUCCESS', NOW() - INTERVAL '5 DAYS'),
    (27, 150.00, 'CARD', 'FAILED', NOW() - INTERVAL '7 DAYS'),
    (33, 220.00, 'UPI', 'SUCCESS', NOW() - INTERVAL '5 DAYS'),
    (41, 100.00, 'CASH', 'SUCCESS', NOW() - INTERVAL '1 DAY');

