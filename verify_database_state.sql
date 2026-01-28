-- SOLUCIÓN DEFINITIVA: Actualizar el checksum de Flyway

-- 1. Ver el estado actual de la migración V10
SELECT * FROM flyway_schema_history WHERE version = '10';

-- 2. Si existe algún registro, eliminarlo completamente
DELETE FROM flyway_schema_history WHERE version = '10';

-- 3. Verificar que se eliminó
SELECT COUNT(*) as registros_v10 FROM flyway_schema_history WHERE version = '10';
-- Debe devolver 0

-- 4. Verificar que la columna y el índice existen
SHOW COLUMNS FROM foods LIKE 'category';
SHOW INDEX FROM foods WHERE Key_name = 'idx_foods_category';

-- 5. Si todo está correcto, ejecuta este mensaje
SELECT 'Base de datos lista. Ahora RENOMBRA el archivo de migración V10 temporalmente' as accion_requerida;
