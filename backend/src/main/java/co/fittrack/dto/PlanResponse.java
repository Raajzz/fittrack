package co.fittrack.dto;

import co.fittrack.model.Goal;
import co.fittrack.model.Macros;
import java.util.List;

/**
 * The crafted meal plan returned to the client.
 *
 * @param maintenanceCalories TDEE — calories to hold current weight.
 * @param target              daily macro target for the chosen goal.
 * @param currentDailyAverage average of what the user actually logged, across logged days.
 * @param dailyAdjustment     target − current. Positive calories = eat more; negative = eat less.
 * @param loggedDays          how many of the 7 days had meals logged (0 → average is a placeholder).
 * @param guidance            plain-language coaching lines ("Eat ~320 kcal more", etc.).
 * @param alternatives        supplement / food swaps to help hit the target.
 */
public record PlanResponse(
        Long userId,
        Goal goal,
        double bmr,
        double maintenanceCalories,
        Macros target,
        Macros currentDailyAverage,
        Macros dailyAdjustment,
        int loggedDays,
        List<String> guidance,
        List<SupplementSuggestion> alternatives) {
}
