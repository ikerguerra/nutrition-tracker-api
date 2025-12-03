-- Create user_profiles table
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    
    -- Personal data (always stored in metric)
    height DECIMAL(10, 2),  -- cm
    weight DECIMAL(10, 2),  -- kg
    date_of_birth DATE,
    gender VARCHAR(10),  -- MALE, FEMALE, OTHER
    
    -- Preferences
    nutritional_goal VARCHAR(30),  -- LOSE_WEIGHT, MAINTAIN, GAIN_MUSCLE, GAIN_WEIGHT
    diet_type VARCHAR(30),  -- STANDARD, KETOGENIC, VEGAN, VEGETARIAN, PALEO, MEDITERRANEAN, LOW_CARB, HIGH_PROTEIN
    activity_level VARCHAR(20),  -- SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTRA_ACTIVE
    preferred_unit_system VARCHAR(10) DEFAULT 'METRIC',  -- METRIC, IMPERIAL
    preferred_language VARCHAR(5) DEFAULT 'es',  -- es, en
    
    -- Calculated goals (in grams/kcal)
    daily_calorie_goal DECIMAL(10, 2),
    daily_protein_goal DECIMAL(10, 2),
    daily_carbs_goal DECIMAL(10, 2),
    daily_fats_goal DECIMAL(10, 2),
    
    -- Custom macros
    use_custom_macros BOOLEAN DEFAULT FALSE,
    custom_protein_percentage DECIMAL(5, 2),  -- 0-100
    custom_carbs_percentage DECIMAL(5, 2),    -- 0-100
    custom_fats_percentage DECIMAL(5, 2),     -- 0-100
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
