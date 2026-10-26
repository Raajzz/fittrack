package co.fittrack.service;

import co.fittrack.model.Goal;
import co.fittrack.model.Macros;
import co.fittrack.model.UserProfile;
import org.springframework.stereotype.Service;

/**
 * Pure, stateless nutrition math. No persistence, no framework coupling beyond the
 * {@code @Service} marker — this is the piece most likely to be reused or ported, so it
 * stays free of I/O.
 *
 * <p>Pipeline: body stats → BMR (Mifflin–St Jeor) → TDEE (× activity) →
 * goal-adjusted target calories → macro split (protein by bodyweight, fat by % of
 * calories, carbs backfilled).
 */
@Service
public class NutritionCalculator {

    /** Fat is set to this fraction of target calories; the rest (after protein) is carbs. */
    private static final double FAT_CALORIE_FRACTION = 0.25;

    /** Mifflin–St Jeor Basal Metabolic Rate (kcal/day). */
    public double bmr(UserProfile p) {
        return 10 * p.getWeightKg()
                + 6.25 * p.getHeightCm()
                - 5 * p.getAge()
                + p.getSex().bmrConstant();
    }

    /** Total Daily Energy Expenditure — maintenance calories. */
    public double tdee(UserProfile p) {
        return bmr(p) * p.getActivityLevel().multiplier();
    }

    /**
     * The daily macro target for this profile's goal.
     *
     * <ul>
     *   <li>Calories = TDEE × goal factor (surplus / deficit / maintenance).</li>
     *   <li>Protein  = bodyweight(kg) × goal's g/kg.</li>
     *   <li>Fat      = 25% of target calories.</li>
     *   <li>Carbs    = whatever calories remain.</li>
     * </ul>
     * Carbs are floored at 0 so extreme inputs never produce a negative target.
     */
    public Macros targetMacros(UserProfile p) {
        Goal goal = p.getGoal();
        double targetCalories = tdee(p) * goal.calorieFactor();

        double proteinG = p.getWeightKg() * goal.proteinPerKg();
        double fatG = (targetCalories * FAT_CALORIE_FRACTION) / 9.0;

        double remainingCalories = targetCalories - (proteinG * 4) - (fatG * 9);
        double carbsG = Math.max(0, remainingCalories / 4.0);

        return new Macros(targetCalories, proteinG, carbsG, fatG).rounded();
    }
}
