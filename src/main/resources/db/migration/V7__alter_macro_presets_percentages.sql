-- Alter macro_presets table to change percentage columns from DECIMAL to INT
-- This migration fixes the schema to match the application's expectations

ALTER TABLE macro_presets 
    MODIFY COLUMN protein_percentage INT NOT NULL,
    MODIFY COLUMN carbs_percentage INT NOT NULL,
    MODIFY COLUMN fats_percentage INT NOT NULL;
