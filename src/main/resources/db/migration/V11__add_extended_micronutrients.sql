-- Add new micronutrients columns to nutritional_info table

ALTER TABLE nutritional_info
ADD COLUMN magnesium DECIMAL(10, 2),
ADD COLUMN zinc DECIMAL(10, 2),
ADD COLUMN vitamin_k DECIMAL(10, 2),
ADD COLUMN vitamin_b1 DECIMAL(10, 2),
ADD COLUMN vitamin_b2 DECIMAL(10, 2),
ADD COLUMN vitamin_b3 DECIMAL(10, 2),
ADD COLUMN vitamin_b6 DECIMAL(10, 2),
ADD COLUMN vitamin_b9 DECIMAL(10, 2);
