-- V16: Insert predefined system meal templates.
-- Creating the tables with IF NOT EXISTS to prevent Flyway from failing before Hibernate
-- runs and creates them. This ensures the inserts can happen safely.
CREATE TABLE IF NOT EXISTS meal_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    meal_type VARCHAR(20),
    is_public BOOLEAN DEFAULT FALSE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_templates_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS meal_template_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_template_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    CONSTRAINT fk_meal_template_items_template FOREIGN KEY (meal_template_id) REFERENCES meal_templates(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_template_items_food FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);

-- Using INSERT IGNORE to be idempotent (safe to re-run).
INSERT IGNORE INTO meal_templates (id, name, description, meal_type, is_public, is_system, user_id, created_at, updated_at) VALUES
(-1, 'Desayuno Proteico Básico', 'Huevos revueltos con tostada y café. Alto en proteínas para empezar el día.', 'BREAKFAST', 1, 1, NULL, NOW(), NOW()),
(-2, 'Avena con Frutas', 'Avena cocida con leche, plátano y nueces. Rico en carbohidratos complejos.', 'BREAKFAST', 1, 1, NULL, NOW(), NOW()),
(-3, 'Almuerzo Rápido: Pollo y Arroz', 'Pechuga de pollo a la plancha con arroz blanco y brocoli.', 'LUNCH', 1, 1, NULL, NOW(), NOW()),
(-4, 'Cena Ligera: Salmón al Horno', 'Salmón horneado con espárragos. Rico en Omega-3.', 'DINNER', 1, 1, NULL, NOW(), NOW()),
(-5, 'Snack: Yogur con Almendras', 'Yogur natural con un puñado de almendras y miel.', 'SNACK', 1, 1, NULL, NOW(), NOW());
