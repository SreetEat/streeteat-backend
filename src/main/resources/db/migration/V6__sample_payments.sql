-- ================================================
-- V6__sample_payments.sql
-- Inserts sample payments for delivered orders
-- ================================================

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

