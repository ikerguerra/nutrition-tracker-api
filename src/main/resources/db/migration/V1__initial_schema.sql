-- Create foods table
CREATE TABLE foods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    barcode VARCHAR(100) UNIQUE,
    serving_size DECIMAL(10, 2),
    serving_unit VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_barcode (barcode),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create nutritional_info table
CREATE TABLE nutritional_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    food_id BIGINT NOT NULL UNIQUE,
    
    -- Macronutrients (per serving)
    calories DECIMAL(10, 2),
    protein DECIMAL(10, 2),
    carbohydrates DECIMAL(10, 2),
    fats DECIMAL(10, 2),
    fiber DECIMAL(10, 2),
    sugars DECIMAL(10, 2),
    saturated_fats DECIMAL(10, 2),
    
    -- Micronutrients (per serving)
    sodium DECIMAL(10, 2),
    calcium DECIMAL(10, 2),
    iron DECIMAL(10, 2),
    potassium DECIMAL(10, 2),
    vitamin_a DECIMAL(10, 2),
    vitamin_c DECIMAL(10, 2),
    vitamin_d DECIMAL(10, 2),
    vitamin_e DECIMAL(10, 2),
    vitamin_b12 DECIMAL(10, 2),
    
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for better query performance
CREATE INDEX idx_food_id ON nutritional_info(food_id);
