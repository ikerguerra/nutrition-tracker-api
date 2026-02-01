CREATE TABLE serving_units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL,
    label VARCHAR(255) NOT NULL,
    weight_grams DECIMAL(10, 2) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    INDEX idx_serving_food_id (food_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
