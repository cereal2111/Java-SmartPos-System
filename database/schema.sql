-- Database Schema for SmartPos (Enterprise Version)
-- WARNING: This script DROPS existing tables. Data will be lost.

DROP DATABASE IF EXISTS smartpos;

CREATE DATABASE smartpos;

USE smartpos;

-- ==========================================
-- 1. CLASSIFIERS (Catalog Tables)
-- ==========================================

CREATE TABLE category (
    category_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (category_id),
    UNIQUE KEY name (name)
);

CREATE TABLE unit_of_measure (
    unit_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    abbreviation VARCHAR(10) NOT NULL,
    PRIMARY KEY (unit_id)
);

CREATE TABLE supplier (
    supplier_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    document VARCHAR(20) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    address VARCHAR(150) DEFAULT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (supplier_id)
);

CREATE TABLE customer (
    customer_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    document VARCHAR(20) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    address VARCHAR(150) DEFAULT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (customer_id)
);

-- ==========================================
-- 2. CORE ENTITIES
-- ==========================================

CREATE TABLE product (
    product_id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    unit_id INT NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    minimum_stock DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (product_id),
    UNIQUE KEY code (code),
    KEY fk_product_category (category_id),
    KEY fk_product_unit (unit_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    CONSTRAINT fk_product_unit FOREIGN KEY (unit_id) REFERENCES unit_of_measure (unit_id)
);

-- ==========================================
-- 3. TRANSACTIONS
-- ==========================================

CREATE TABLE sale (
    sale_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    customer_id INT NOT NULL,
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('REGISTERED', 'CANCELLED') NOT NULL DEFAULT 'REGISTERED',
    PRIMARY KEY (sale_id),
    KEY fk_sale_customer (customer_id),
    KEY fk_sale_user (user_id),
    CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE sale_item (
    sale_item_id INT NOT NULL AUTO_INCREMENT,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (sale_item_id),
    KEY fk_sale_item_sale (sale_id),
    KEY idx_sale_item_product (product_id),
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES sale (sale_id)
);

CREATE TABLE purchase (
    purchase_id INT NOT NULL AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    purchase_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('REGISTERED', 'CANCELLED') NOT NULL DEFAULT 'REGISTERED',
    PRIMARY KEY (purchase_id),
    KEY fk_purchase_supplier (supplier_id),
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id)
);

CREATE TABLE purchase_item (
    purchase_item_id INT NOT NULL AUTO_INCREMENT,
    purchase_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_cost DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (purchase_item_id),
    KEY fk_purchase_item_purchase (purchase_id),
    KEY idx_purchase_item_product (product_id),
    CONSTRAINT fk_purchase_item_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT fk_purchase_item_purchase FOREIGN KEY (purchase_id) REFERENCES purchase (purchase_id)
);

CREATE TABLE inventory_movement (
    movement_id INT NOT NULL AUTO_INCREMENT,
    product_id INT NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (movement_id),
    KEY idx_inventory_product (product_id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

-- ==========================================
-- 4. USERS & SECURITY
-- ==========================================

CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'CASHIER',
    active TINYINT(1) DEFAULT 1,
    PRIMARY KEY (user_id),
    UNIQUE KEY username (username)
);

CREATE TABLE cash_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    opening_balance DECIMAL(12, 2) DEFAULT 0,
    total_sales DECIMAL(12, 2) DEFAULT 0,
    actual_cash DECIMAL(12, 2) DEFAULT 0,
    status VARCHAR(20) DEFAULT 'OPEN',
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- ==========================================
-- 5. VIEWS
-- ==========================================

CREATE OR REPLACE VIEW v_current_stock AS
SELECT
    p.product_id,
    p.name AS product_name,
    COALESCE(
        SUM(
            CASE
                WHEN m.movement_type = 'IN' THEN m.quantity
                WHEN m.movement_type = 'OUT' THEN - m.quantity
                ELSE 0
            END
        ),
        0
    ) AS current_stock
FROM
    product p
    LEFT JOIN inventory_movement m ON p.product_id = m.product_id
GROUP BY
    p.product_id,
    p.name;

-- ==========================================
-- 6. SEED DATA (Crucial for App Functions)
-- ==========================================

-- Default Units
INSERT INTO
    unit_of_measure (name, abbreviation)
VALUES ('Unit', 'UN'),
    ('Kilogram', 'KG'),
    ('Liter', 'L');

-- Default Categories
INSERT INTO
    category (name)
VALUES ('General'),
    ('Beverages'),
    ('Snacks');

-- Default Supplier
INSERT INTO
    supplier (name, phone, email)
VALUES (
        'Local Supplier',
        '555-0199',
        'supplier@local.com'
    );

-- Default Customer (Walk-in client)
INSERT INTO
    customer (name, document)
VALUES (
        'Walk-in Customer',
        '00000000'
    );

-- Default Product
INSERT INTO
    product (
        code,
        name,
        category_id,
        unit_id,
        sale_price,
        minimum_stock
    )
VALUES (
        '1001',
        'Cola Soda 500ml',
        2,
        1,
        1.50,
        10.00
    );

-- Initial Stock for Demo
INSERT INTO
    inventory_movement (
        product_id,
        movement_type,
        quantity,
        reference_type
    )
VALUES (1, 'IN', 50, 'ADJUSTMENT');

-- Default Users (Will be auto-hashed on first login)
INSERT INTO
    users (username, password_hash, role)
VALUES ('admin', 'admin123', 'ADMIN'),
    (
        'cashier',
        'cashier123',
        'CASHIER'
    );