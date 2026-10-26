package co.fittrack.service;

import co.fittrack.dto.PlanResponse;
import co.fittrack.model.DietDay;
import co.fittrack.model.Macros;
import co.fittrack.model.UserProfile;
import co.fittrack.repository.DietDayRepository;
import co.fittrack.repository.UserProfileRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Crafts the meal plan: compares the target for the user's goal against what they
 * actually logged, and produces per-macro adjustments, coaching lines, and
 * supplement alternatives.
 */
@Service
public class PlanService {

    private final UserProfileRepository profiles;
    private final DietDayRepository dietDays;
    private final NutritionCalculator calculator;
    private final SupplementService supplements;

    public PlanService(UserProfileRepository profiles,
                       DietDayRepository dietDays,
                       NutritionCalculator calculator,
                       SupplementService supplements) {
        this.profiles = profiles;
        this.dietDays = dietDays;
        this.calculator = calculator;
        this.supplements = supplements;
    }

    public PlanResponse buildPlan(Long userId) {
        UserProfile profile = profiles.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        Macros target = calculator.targetMacros(profile);

        // Average intake across the days that actually have meals logged.
        List<DietDay> days = dietDays.findByUserId(userId);
        List<DietDay> loggedDays = days.stream()
                .filter(d -> !d.getMeals().isEmpty())
                .toList();

        Macros average = Macros.ZERO;
        if (!loggedDays.isEmpty()) {
            Macros sum = Macros.ZERO;
            for (DietDay d : loggedDays) {
                sum = sum.plus(d.totalMacros());
            }
            average = sum.scale(1.0 / loggedDays.size()).rounded();
        }

        Macros adjustment = target.minus(average).rounded();

        List<String> guidance = buildGuidance(target, average, adjustment, loggedDays.size());

        return new PlanResponse(
                userId,
                profile.getGoal(),
                Math.round(calculator.bmr(profile)),
                Math.round(calculator.tdee(profile)),
                target,
                average,
                adjustment,
                loggedDays.size(),
                guidance,
                supplements.suggest(adjustment));
    }

    private List<String> buildGuidance(Macros target, Macros current, Macros adj, int loggedDays) {
        List<String> lines = new ArrayList<>();

        if (loggedDays == 0) {
            lines.add("Log at least one day of meals to see how your current intake compares to your target.");
            lines.add(String.format("Your daily target is %.0f kcal — %.0fg protein, %.0fg carbs, %.0fg fat.",
                    target.calories(), target.protein(), target.carbs(), target.fat()));
            return lines;
        }

        lines.add(String.format("Logged %d day%s. Daily average: %.0f kcal (%.0fP / %.0fC / %.0fF).",
                loggedDays, loggedDays == 1 ? "" : "s",
                current.calories(), current.protein(), current.carbs(), current.fat()));

        lines.add(caloriesLine(adj.calories()));
        lines.add(macroLine("protein", adj.protein()));
        lines.add(macroLine("carbs", adj.carbs()));
        lines.add(macroLine("fat", adj.fat()));
        return lines;
    }

    private String caloriesLine(double delta) {
        if (Math.abs(delta) < 75) {
            return "Calories are on target — nice, hold steady.";
        }
        return delta > 0
                ? String.format("Eat about %.0f kcal MORE per day to reach your goal.", delta)
                : String.format("Eat about %.0f kcal LESS per day to reach your goal.", Math.abs(delta));
    }

    private String macroLine(String name, double delta) {
        if (Math.abs(delta) < 10) {
            return String.format("%s is roughly on target.", capitalize(name));
        }
        return delta > 0
                ? String.format("Add ~%.0fg %s per day.", delta, name)
                : String.format("Cut ~%.0fg %s per day.", Math.abs(delta), name);
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
