package co.fittrack.model;

/**
 * Immutable nutrition totals. All values are per-day unless stated otherwise.
 * Calories are kcal; protein / carbs / fat are grams.
 */
public record Macros(double calories, double protein, double carbs, double fat) {

    public static final Macros ZERO = new Macros(0, 0, 0, 0);

    /** Atwater factors: 4 kcal/g protein, 4 kcal/g carbs, 9 kcal/g fat. */
    public static Macros fromGrams(double protein, double carbs, double fat) {
        double calories = protein * 4 + carbs * 4 + fat * 9;
        return new Macros(calories, protein, carbs, fat);
    }

    public Macros plus(Macros other) {
        return new Macros(
                calories + other.calories,
                protein + other.protein,
                carbs + other.carbs,
                fat + other.fat);
    }

    public Macros minus(Macros other) {
        return new Macros(
                calories - other.calories,
                protein - other.protein,
                carbs - other.carbs,
                fat - other.fat);
    }

    public Macros scale(double factor) {
        return new Macros(calories * factor, protein * factor, carbs * factor, fat * factor);
    }

    public Macros rounded() {
        return new Macros(
                Math.round(calories),
                Math.round(protein),
                Math.round(carbs),
                Math.round(fat));
    }
}
