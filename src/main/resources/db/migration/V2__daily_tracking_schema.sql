-- Create daily_logs table
CREATE TABLE daily_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT, -- For future auth
    date DATE NOT NULL,
    
    -- Aggregated totals
    total_calories DECIMAL(10, 2) DEFAULT 0.00,
    total_protein DECIMAL(10, 2) DEFAULT 0.00,
    total_carbs DECIMAL(10, 2) DEFAULT 0.00,
    total_fats DECIMAL(10, 2) DEFAULT 0.00,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Ensure one log per user per day
    UNIQUE KEY uk_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create meal_entries table
CREATE TABLE meal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    daily_log_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    
    meal_type VARCHAR(20) NOT NULL, -- BREAKFAST, LUNCH, DINNER, SNACK
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    
    -- Snapshot of nutritional values (calculated based on quantity)
    calories DECIMAL(10, 2) NOT NULL,
    protein DECIMAL(10, 2) NOT NULL,
    carbohydrates DECIMAL(10, 2) NOT NULL,
    fats DECIMAL(10, 2) NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (daily_log_id) REFERENCES daily_logs(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes
CREATE INDEX idx_daily_log_date ON daily_logs(date);
CREATE INDEX idx_meal_entry_log_id ON meal_entries(daily_log_id);
