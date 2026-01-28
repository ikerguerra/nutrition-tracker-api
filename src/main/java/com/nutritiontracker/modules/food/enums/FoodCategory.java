package com.nutritiontracker.modules.food.enums;

/**
 * Food categories for classification and filtering
 */
public enum FoodCategory {
    FRUITS("Frutas"),
    VEGETABLES("Verduras"),
    GRAINS("Cereales y Granos"),
    PROTEIN("Proteínas"),
    DAIRY("Lácteos"),
    FATS_OILS("Grasas y Aceites"),
    SWEETS("Dulces y Postres"),
    BEVERAGES("Bebidas"),
    SNACKS("Snacks"),
    PREPARED_MEALS("Comidas Preparadas"),
    LEGUMES("Legumbres"),
    NUTS_SEEDS("Frutos Secos y Semillas"),
    CONDIMENTS("Condimentos y Salsas"),
    OTHER("Otros");

    private final String displayName;

    FoodCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
