package co.fittrack.model;

/** Standard activity multipliers applied to BMR to estimate TDEE (maintenance calories). */
public enum ActivityLevel {
    SEDENTARY(1.20),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.90);

    private final double multiplier;

    ActivityLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
