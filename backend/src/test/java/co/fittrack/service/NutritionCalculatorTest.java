package co.fittrack.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.fittrack.model.ActivityLevel;
import co.fittrack.model.Goal;
import co.fittrack.model.Macros;
import co.fittrack.model.Sex;
import co.fittrack.model.UserProfile;
import org.junit.jupiter.api.Test;

class NutritionCalculatorTest {

    private final NutritionCalculator calc = new NutritionCalculator();

    private UserProfile sampleMale(Goal goal) {
        UserProfile p = new UserProfile();
        p.setSex(Sex.MALE);
        p.setAge(30);
        p.setHeightCm(180);
        p.setWeightKg(80);
        p.setActivityLevel(ActivityLevel.MODERATE);
        p.setGoal(goal);
        return p;
    }

    @Test
    void bmrMatchesMifflinStJeor() {
        // 10*80 + 6.25*180 - 5*30 + 5 = 800 + 1125 - 150 + 5 = 1780
        assertEquals(1780.0, calc.bmr(sampleMale(Goal.RECOMP)), 0.001);
    }

    @Test
    void tdeeAppliesActivityMultiplier() {
        // 1780 * 1.55 = 2759
        assertEquals(2759.0, calc.tdee(sampleMale(Goal.RECOMP)), 0.001);
    }

    @Test
    void bulkTargetIsASurplus() {
        double maintenance = calc.tdee(sampleMale(Goal.BULK));
        Macros target = calc.targetMacros(sampleMale(Goal.BULK));
        assertTrue(target.calories() > maintenance, "bulk should be above maintenance");
    }

    @Test
    void cutTargetIsADeficit() {
        double maintenance = calc.tdee(sampleMale(Goal.CUT));
        Macros target = calc.targetMacros(sampleMale(Goal.CUT));
        assertTrue(target.calories() < maintenance, "cut should be below maintenance");
    }

    @Test
    void proteinScalesWithBodyweightAndGoal() {
        // CUT = 2.2 g/kg * 80kg = 176g
        Macros target = calc.targetMacros(sampleMale(Goal.CUT));
        assertEquals(176.0, target.protein(), 1.0);
    }

    @Test
    void carbsNeverNegative() {
        UserProfile tiny = sampleMale(Goal.CUT);
        tiny.setWeightKg(40);
        tiny.setHeightCm(150);
        tiny.setActivityLevel(ActivityLevel.SEDENTARY);
        assertTrue(calc.targetMacros(tiny).carbs() >= 0);
    }
}
