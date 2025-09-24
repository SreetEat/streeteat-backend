-- ===============================
-- Flyway Migration: Initial Schema
-- Version: V1
-- ===============================

-- Users table (includes Customer + Admin roles)
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL, -- CUSTOMER, VENDOR, DELIVERY_PARTNER, ADMIN
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vendors table
CREATE TABLE vendors (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         email VARCHAR(100) UNIQUE NOT NULL,
                         phone VARCHAR(20),
                         address TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Delivery Partners table
CREATE TABLE delivery_partners (
                                   id BIGSERIAL PRIMARY KEY,
                                   name VARCHAR(100) NOT NULL,
                                   phone VARCHAR(20),
                                   vehicle_number VARCHAR(50),
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        vendor_id BIGINT NOT NULL REFERENCES vendors(id) ON DELETE CASCADE,
                        delivery_partner_id BIGINT REFERENCES delivery_partners(id) ON DELETE SET NULL,
                        status VARCHAR(50) NOT NULL, -- PENDING, ACCEPTED, ASSIGNED, DELIVERED, CANCELLED
                        total_amount DECIMAL(10,2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order Items table
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             item_name VARCHAR(100) NOT NULL,
                             quantity INT NOT NULL,
                             price DECIMAL(10,2) NOT NULL
);
