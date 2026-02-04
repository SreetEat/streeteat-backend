-- ================================================
-- V4__sample_menu_items.sql
-- Inserts sample menu items for vendors
-- ================================================

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
