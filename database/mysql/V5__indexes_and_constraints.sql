CREATE INDEX idx_inventory_product
    ON inventory_movement(product_id);

CREATE INDEX idx_sale_created_at
    ON sale(created_at);

CREATE INDEX idx_purchase_created_at
    ON purchase(created_at);
