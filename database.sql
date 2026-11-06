-- AniLink Database Schema
-- Local Farmer Product Marketplace and Management System

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50) NULL,
    last_name VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'farmer', 'buyer', 'rider') DEFAULT 'buyer',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Farmers table
CREATE TABLE IF NOT EXISTS farmers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    contact_number VARCHAR(20),
    farm_address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Buyers table
CREATE TABLE IF NOT EXISTS buyers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    contact_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Extend users role enum with rider (for databases created before rider support)
ALTER TABLE users MODIFY role ENUM('admin', 'farmer', 'buyer', 'rider') DEFAULT 'buyer';

-- Riders table (delivery staff)
CREATE TABLE IF NOT EXISTS riders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    contact_number VARCHAR(20),
    vehicle_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    farmer_id INT NOT NULL,
    category_id INT,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    image VARCHAR(255),
    status ENUM('available', 'out_of_stock', 'inactive') DEFAULT 'available',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    rider_id INT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    delivery_method ENUM('pickup', 'delivery') NOT NULL,
    delivery_address TEXT,
    payment_method VARCHAR(50),
    status ENUM('pending', 'preparing', 'ready_for_pickup', 'handed_to_rider', 'on_the_way', 'completed', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE,
    FOREIGN KEY (rider_id) REFERENCES riders(id) ON DELETE SET NULL
);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    status ENUM('pending', 'confirmed', 'preparing', 'ready', 'completed', 'cancelled') NOT NULL DEFAULT 'pending',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Persistent cart table (per-buyer, survives logout/restart)
CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    UNIQUE KEY uq_buyer_product (buyer_id, product_id),
    FOREIGN KEY (buyer_id) REFERENCES buyers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Payment records (one per order; UNIQUE prevents duplicate payments)
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    reference_no VARCHAR(100) NULL,
    proof_image VARCHAR(255) NULL,
    status ENUM('pending', 'verified', 'rejected') NOT NULL DEFAULT 'pending',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_payment_order (order_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Admin-configured online payment details
CREATE TABLE IF NOT EXISTS payment_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(100) NOT NULL DEFAULT 'GCash',
    account_name VARCHAR(100) NULL,
    account_number VARCHAR(50) NULL,
    instructions TEXT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default categories
INSERT IGNORE INTO categories (category_name) VALUES 
('Vegetables'),
('Fruits'),
('Rice'),
('Eggs'),
('Meat'),
('Fish'),
('Root Crops'),
('Grains'),
('Herbs'),
('Other Farm Products');

-- Insert default admin user (password: admin123, hashed with password_hash)
INSERT IGNORE INTO users (first_name, middle_name, last_name, name, email, password, role) VALUES 
('System', NULL, 'Admin', 'System Admin', 'admin@anilink.com', '$2y$10$Nv8xO5y2w4dEBL/KawqXzuDlf8lsFrOos4Zjfz6UGWnwPdQvq.pgC', 'admin');

-- Insert default farmer user (password: farmer123, hashed with password_hash)
INSERT IGNORE INTO users (first_name, middle_name, last_name, name, email, password, role) VALUES 
('Demo', NULL, 'Farmer', 'Demo Farmer', 'farmer@anilink.com', '$2y$10$OBEbKA2KTmv4WRg6qJvqYe.77Naq7kNdVzfAMo9cTSowmO9UbqEhC', 'farmer');

-- Insert default buyer user (password: buyer123, hashed with password_hash)
INSERT IGNORE INTO users (first_name, middle_name, last_name, name, email, password, role) VALUES 
('Demo', NULL, 'Buyer', 'Demo Buyer', 'buyer@anilink.com', '$2y$10$sQQ55HBAoO3.KMYVS1VyFeLVSvSfXpl.TIlx8e2gqTkz1Sl80qNqC', 'buyer');

-- Link sample farmer to farmer user (user_id 2)
INSERT INTO farmers (user_id, contact_number, farm_address)
SELECT u.id, '09171234567', 'Barangay San Isidro, Nueva Ecija'
FROM users u WHERE u.email = 'farmer@anilink.com'
AND NOT EXISTS (SELECT 1 FROM farmers f WHERE f.user_id = u.id);

-- Link sample buyer to buyer user (user_id 3)
INSERT INTO buyers (user_id, contact_number, address)
SELECT u.id, '09187654321', '123 Rizal Street, Quezon City'
FROM users u WHERE u.email = 'buyer@anilink.com'
AND NOT EXISTS (SELECT 1 FROM buyers b WHERE b.user_id = u.id);

-- Sample products for the demo farmer
INSERT INTO products (farmer_id, category_id, product_name, description, price, unit, quantity, status)
SELECT f.id, c.id, s.product_name, s.description, s.price, s.unit, s.quantity, 'available'
FROM farmers f
JOIN users u ON f.user_id = u.id
JOIN categories c ON c.category_name = s.category_name
JOIN (
    SELECT 'Vegetables' AS category_name, 'Fresh Tomatoes' AS product_name, 'Fresh locally grown tomatoes.' AS description, 80.00 AS price, 'kg' AS unit, 20 AS quantity
    UNION ALL SELECT 'Vegetables', 'Fresh Eggplant', 'Fresh purple eggplant harvested daily.', 70.00, 'kg', 15
    UNION ALL SELECT 'Fruits', 'Carabao Mango', 'Sweet and ripe carabao mangoes.', 120.00, 'kg', 30
    UNION ALL SELECT 'Rice', 'Local Rice', 'Premium local white rice.', 55.00, 'kg', 100
    UNION ALL SELECT 'Eggs', 'Fresh Eggs', 'Fresh farm eggs collected daily.', 8.00, 'piece', 200
    UNION ALL SELECT 'Meat', 'Fresh Chicken', 'Native free-range chicken.', 220.00, 'kg', 10
    UNION ALL SELECT 'Root Crops', 'Sweet Potato', 'Fresh orange sweet potato.', 60.00, 'kg', 25
    UNION ALL SELECT 'Vegetables', 'Bitter Gourd', 'Fresh ampalaya for healthy meals.', 65.00, 'kg', 3
) AS s ON u.email = 'farmer@anilink.com'
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.product_name = s.product_name AND p.farmer_id = f.id);

-- Default online payment details (editable by admin in Payment Management)
INSERT INTO payment_settings (provider, account_name, account_number, instructions)
SELECT 'GCash', 'AniLink Marketplace', '09170000000',
       'Send the exact amount to the GCash number above, then submit your GCash reference number and a screenshot of the receipt as payment proof.'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM payment_settings);