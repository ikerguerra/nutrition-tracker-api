# Database Schema

## Overview

PostgreSQL 14+ con Flyway para migraciones versionadas.

## Connection

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nutrition_tracker_db
    username: postgres
    password: your_password
```

## Schema Diagram

```
┌──────────────┐       ┌──────────────────┐
│    users     │───────│  user_profiles   │
└──────────────┘  1:1  └──────────────────┘
       │
       │ 1:N
       ▼
┌──────────────┐       ┌──────────────────┐
│ daily_logs   │───────│  meal_entries    │
└──────────────┘  1:N  └──────────────────┘
                              │
                              │ N:1
                              ▼
                       ┌──────────────┐
                       │    foods     │
                       └──────────────┘
```

## Tables

### users

Usuarios del sistema.

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    provider VARCHAR(20) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

**Campos:**
- `id`: Identificador único
- `name`: Nombre completo
- `email`: Email único (usado para login)
- `password`: Hash BCrypt de la contraseña
- `provider`: Proveedor de auth (LOCAL, GOOGLE, FACEBOOK)
- `provider_id`: ID del proveedor OAuth2
- `enabled`: Usuario activo/inactivo
- `created_at`: Fecha de creación
- `updated_at`: Última actualización

### user_profiles

Perfiles de usuario con datos personales y objetivos.

```sql
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id),
    height DECIMAL(10,2),
    weight DECIMAL(10,2),
    date_of_birth DATE,
    gender VARCHAR(10),
    nutritional_goal VARCHAR(30),
    diet_type VARCHAR(30),
    activity_level VARCHAR(20),
    preferred_unit_system VARCHAR(10) DEFAULT 'METRIC',
    preferred_language VARCHAR(5) DEFAULT 'es',
    daily_calorie_goal DECIMAL(10,2),
    daily_protein_goal DECIMAL(10,2),
    daily_carbs_goal DECIMAL(10,2),
    daily_fats_goal DECIMAL(10,2),
    use_custom_macros BOOLEAN DEFAULT FALSE,
    custom_protein_percentage DECIMAL(5,2),
    custom_carbs_percentage DECIMAL(5,2),
    custom_fats_percentage DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

**Campos Calculados:**
- `daily_calorie_goal`: Calculado automáticamente basado en TMB/TDEE
- `daily_protein_goal`: Gramos de proteína diarios
- `daily_carbs_goal`: Gramos de carbohidratos diarios
- `daily_fats_goal`: Gramos de grasas diarias

### foods

Catálogo de alimentos (personalizados y de OpenFoodFacts).

```sql
CREATE TABLE foods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    barcode VARCHAR(50),
    serving_size DECIMAL(10,2) NOT NULL,
    serving_unit VARCHAR(20) NOT NULL,
    calories DECIMAL(10,2) NOT NULL,
    protein DECIMAL(10,2) NOT NULL,
    carbs DECIMAL(10,2) NOT NULL,
    fats DECIMAL(10,2) NOT NULL,
    fiber DECIMAL(10,2),
    sugar DECIMAL(10,2),
    sodium DECIMAL(10,2),
    user_id BIGINT REFERENCES users(id),
    is_public BOOLEAN DEFAULT FALSE,
    source VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_foods_name ON foods(name);
CREATE INDEX idx_foods_barcode ON foods(barcode);
CREATE INDEX idx_foods_user_id ON foods(user_id);
```

**Campos:**
- `user_id`: NULL para alimentos públicos, ID para personalizados
- `is_public`: Si el alimento es visible para todos
- `source`: Origen (USER, OPENFOODFACTS, SYSTEM)
- `barcode`: Código de barras (para productos comerciales)

### daily_logs

Registro diario de comidas por usuario.

```sql
CREATE TABLE daily_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    total_calories DECIMAL(10,2) DEFAULT 0,
    total_protein DECIMAL(10,2) DEFAULT 0,
    total_carbs DECIMAL(10,2) DEFAULT 0,
    total_fats DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(user_id, date)
);

CREATE INDEX idx_daily_logs_user_date ON daily_logs(user_id, date);
```

**Totales:**
Los campos `total_*` se actualizan automáticamente al agregar/eliminar entradas.

### meal_entries

Entradas individuales de comidas en un log diario.

```sql
CREATE TABLE meal_entries (
    id BIGSERIAL PRIMARY KEY,
    daily_log_id BIGINT NOT NULL REFERENCES daily_logs(id) ON DELETE CASCADE,
    food_id BIGINT NOT NULL REFERENCES foods(id),
    meal_type VARCHAR(30) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    calories DECIMAL(10,2) NOT NULL,
    protein DECIMAL(10,2) NOT NULL,
    carbs DECIMAL(10,2) NOT NULL,
    fats DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_meal_entries_daily_log ON meal_entries(daily_log_id);
CREATE INDEX idx_meal_entries_food ON meal_entries(food_id);
```

**Campos:**
- `meal_type`: BREAKFAST, LUNCH, DINNER, SNACK, etc.
- `quantity`: Cantidad en unidades del alimento
- Campos nutricionales calculados según cantidad

### macro_presets

Presets de macronutrientes guardados por usuario.

```sql
CREATE TABLE macro_presets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    protein_percentage DECIMAL(5,2) NOT NULL,
    carbs_percentage DECIMAL(5,2) NOT NULL,
    fats_percentage DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_macro_presets_user ON macro_presets(user_id);
```

## Migrations

### Naming Convention

```
V{version}__{description}.sql
```

Ejemplos:
- `V1__initial_schema.sql`
- `V2__add_users_table.sql`
- `V3__user_schema.sql`
- `V4__user_profile_schema.sql`

### Migration Files

Located in: `src/main/resources/db/migration/`

#### V1__initial_schema.sql
- Crea tablas `foods`, `daily_logs`, `meal_entries`

#### V2__add_users_table.sql
- Crea tabla `users`
- Añade `user_id` a `foods` y `daily_logs`

#### V3__user_schema.sql
- Añade campos OAuth2 a `users`
- Crea índices

#### V4__user_profile_schema.sql
- Crea tabla `user_profiles`
- Crea tabla `macro_presets`
- Añade campos calculados

### Running Migrations

```bash
# Aplicar migraciones pendientes
mvn flyway:migrate

# Ver estado
mvn flyway:info

# Limpiar base de datos (CUIDADO!)
mvn flyway:clean
```

## Indexes

### Performance Indexes

```sql
-- Búsqueda de alimentos
CREATE INDEX idx_foods_name ON foods(name);
CREATE INDEX idx_foods_barcode ON foods(barcode);

-- Logs por usuario y fecha
CREATE INDEX idx_daily_logs_user_date ON daily_logs(user_id, date);

-- Entradas de comidas
CREATE INDEX idx_meal_entries_daily_log ON meal_entries(daily_log_id);
```

## Constraints

### Foreign Keys

Todas las relaciones tienen foreign keys con:
- `ON DELETE CASCADE` para entradas dependientes
- `ON DELETE RESTRICT` para referencias importantes

### Unique Constraints

- `users.email` - Email único
- `user_profiles.user_id` - Un perfil por usuario
- `daily_logs(user_id, date)` - Un log por usuario por día

### Check Constraints

```sql
-- Validar porcentajes de macros
ALTER TABLE user_profiles 
ADD CONSTRAINT check_protein_percentage 
CHECK (custom_protein_percentage >= 0 AND custom_protein_percentage <= 100);

-- Validar valores nutricionales positivos
ALTER TABLE foods 
ADD CONSTRAINT check_positive_calories 
CHECK (calories >= 0);
```

## Queries Comunes

### Obtener log del día con entradas

```sql
SELECT 
    dl.*,
    me.id as entry_id,
    me.meal_type,
    me.quantity,
    f.name as food_name
FROM daily_logs dl
LEFT JOIN meal_entries me ON me.daily_log_id = dl.id
LEFT JOIN foods f ON f.id = me.food_id
WHERE dl.user_id = ? AND dl.date = ?
ORDER BY me.created_at;
```

### Calcular totales de un log

```sql
SELECT 
    SUM(calories) as total_calories,
    SUM(protein) as total_protein,
    SUM(carbs) as total_carbs,
    SUM(fats) as total_fats
FROM meal_entries
WHERE daily_log_id = ?;
```

### Buscar alimentos

```sql
SELECT * FROM foods
WHERE (user_id = ? OR is_public = TRUE)
  AND name ILIKE ?
ORDER BY name
LIMIT 20 OFFSET 0;
```

## Backup & Restore

### Backup

```bash
pg_dump -U postgres nutrition_tracker_db > backup.sql
```

### Restore

```bash
psql -U postgres nutrition_tracker_db < backup.sql
```

## Monitoring

### Table Sizes

```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Index Usage

```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```
