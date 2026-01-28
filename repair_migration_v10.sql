-- Script para reparar la migración fallida de Flyway
-- Ejecuta este script en MySQL Workbench o tu cliente MySQL preferido
-- Base de datos: nutrition_tracker_db

-- 1. Eliminar el registro de la migración fallida
DELETE FROM flyway_schema_history WHERE version = '10';

-- 2. Verificar si la columna ya existe
SELECT COUNT(*) as column_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'nutrition_tracker_db' 
  AND TABLE_NAME = 'foods' 
  AND COLUMN_NAME = 'category';

-- 3. Si la columna NO existe (column_exists = 0), ejecuta esto:
ALTER TABLE foods ADD COLUMN category VARCHAR(50);

-- 4. Crear índice si no existe
CREATE INDEX idx_foods_category ON foods(category);

-- 5. Crear índices para filtros nutricionales (para uso futuro)
CREATE INDEX idx_nutritional_info_protein ON nutritional_info(protein);
CREATE INDEX idx_nutritional_info_carbs ON nutritional_info(carbs);
CREATE INDEX idx_nutritional_info_fats ON nutritional_info(fats);
CREATE INDEX idx_nutritional_info_calories ON nutritional_info(calories);

-- 6. Establecer categoría por defecto para alimentos existentes
UPDATE foods SET category = 'OTHER' WHERE category IS NULL;

-- 7. Verificar que todo está correcto
SELECT 'Migration repair completed successfully!' as status;
SELECT COUNT(*) as total_foods, 
       COUNT(category) as foods_with_category 
FROM foods;
