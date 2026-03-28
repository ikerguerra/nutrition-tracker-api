-- Modificar food_id para que sea opcional, ya que las entradas (MealEntry) ahora pueden apuntar a food_id o recipe_id
ALTER TABLE meal_entries MODIFY COLUMN food_id BIGINT NULL;
