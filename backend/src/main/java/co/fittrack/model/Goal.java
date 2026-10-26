package co.fittrack.model;

/**
 * The body-composition objective that drives every calorie / macro decision.
 *
 * <p>Each goal carries the two levers the nutrition engine needs:
 * <ul>
 *   <li>{@code calorieFactor} – multiplier applied to maintenance (TDEE).
 *       &gt;1 = surplus (bulk), &lt;1 = deficit (cut), =1 = maintenance (recomp).</li>
 *   <li>{@code proteinPerKg} – grams of protein per kg of bodyweight. Higher when
 *       in a deficit or recomping to protect / build lean mass.</li>
 * </ul>
 */
public enum Goal {
    BULK(1.12, 1.8),
    CUT(0.78, 2.2),
    RECOMP(1.00, 2.2);

    private final double calorieFactor;
    private final double proteinPerKg;

    Goal(double calorieFactor, double proteinPerKg) {
        this.calorieFactor = calorieFactor;
        this.proteinPerKg = proteinPerKg;
    }

    public double calorieFactor() {
        return calorieFactor;
    }

    public double proteinPerKg() {
        return proteinPerKg;
    }
}
