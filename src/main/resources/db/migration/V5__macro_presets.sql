-- Create macro_presets table
CREATE TABLE macro_presets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    protein_percentage INT NOT NULL,  -- 0-100
    carbs_percentage INT NOT NULL,    -- 0-100
    fats_percentage INT NOT NULL,     -- 0-100
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    
    -- Ensure sum of percentages is 100 (checked in application layer)
    CONSTRAINT chk_percentages CHECK (
        protein_percentage >= 0 AND protein_percentage <= 100 AND
        carbs_percentage >= 0 AND carbs_percentage <= 100 AND
        fats_percentage >= 0 AND fats_percentage <= 100
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
