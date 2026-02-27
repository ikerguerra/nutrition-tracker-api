-- Migration to ensure core macronutrients are non-null and have default values
-- Update existing null values to 0.00
UPDATE nutritional_info SET calories = 0.00 WHERE calories IS NULL;
UPDATE nutritional_info SET protein = 0.00 WHERE protein IS NULL;
UPDATE nutritional_info SET carbohydrates = 0.00 WHERE carbohydrates IS NULL;
UPDATE nutritional_info SET fats = 0.00 WHERE fats IS NULL;

-- Alter table to add NOT NULL constraints and DEFAULT 0.00
ALTER TABLE nutritional_info 
    MODIFY COLUMN calories DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    MODIFY COLUMN protein DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    MODIFY COLUMN carbohydrates DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    MODIFY COLUMN fats DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
