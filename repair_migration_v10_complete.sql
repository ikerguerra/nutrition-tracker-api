-- Script completo de reparación - Ejecutar TODO en MySQL Workbench

-- 1. Ver el estado actual de flyway_schema_history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 2. Eliminar TODAS las entradas de la versión 10 (por si hay múltiples)
DELETE FROM flyway_schema_history WHERE version = '10';

-- 3. Verificar que se eliminó
SELECT * FROM flyway_schema_history WHERE version = '10';
-- Debe devolver 0 filas

-- 4. Verificar si la columna category existe
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'nutrition_tracker_db' 
  AND TABLE_NAME = 'foods' 
  AND COLUMN_NAME = 'category';

-- 5. Si la columna NO existe, créala:
-- ALTER TABLE foods ADD COLUMN category VARCHAR(50);

-- 6. Verificar índices existentes
SHOW INDEX FROM foods WHERE Key_name = 'idx_foods_category';

-- 7. Si el índice no existe, créalo:
-- CREATE INDEX idx_foods_category ON foods(category);

-- 8. Actualizar alimentos sin categoría
UPDATE foods SET category = 'OTHER' WHERE category IS NULL OR category = '';

-- 9. Verificación final
SELECT 
    COUNT(*) as total_foods,
    COUNT(category) as foods_with_category,
    COUNT(CASE WHEN category IS NULL THEN 1 END) as foods_without_category
FROM foods;

SELECT 'Reparación completada. Ahora ejecuta: mvn spring-boot:run' as mensaje;
