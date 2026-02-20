package com.nutritiontracker.modules.achievement.enums;

public enum AchievementType {
    // Rachas de registro
    STREAK_3("Racha de 3 días", 3),
    STREAK_7("Racha de 7 días", 7),
    STREAK_30("Racha de 30 días", 30),
    STREAK_100("Racha de 100 días", 100),

    // Primer registro
    FIRST_LOG("Primer registro diario", 1),
    FIRST_GOAL("Primer objetivo cumplido", 1),

    // Peso
    WEIGHT_LOGGED("Primer registro de peso", 1),
    WEIGHT_MILESTONE("Hito de pérdida de peso", 1),

    // Constancia semanal
    CONSISTENCY_WEEK("Semana completa registrada", 7),
    CONSISTENCY_MONTH("Mes completo registrado", 30),

    // Exploración de funciones
    FIRST_RECIPE("Primera receta creada", 1),
    FIRST_TEMPLATE("Primera plantilla creada", 1),
    FIRST_RECOMMENDATION("Primera recomendación aceptada", 1);

    private final String displayName;
    private final int target;

    AchievementType(String displayName, int target) {
        this.displayName = displayName;
        this.target = target;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getTarget() {
        return target;
    }
}
