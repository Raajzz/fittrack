package co.fittrack.service;

import co.fittrack.dto.SupplementSuggestion;
import co.fittrack.model.Macros;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Turns a macro gap into concrete "alternatives" — the "if you can't eat that much,
 * reach for a supplement" feature.
 *
 * <p>Suggestions are driven by {@code adjustment = target − current}:
 * positive values mean the user needs to add intake, negative means trim it.
 */
@Service
public class SupplementService {

    // Reference servings (approximate, per typical serving).
    private static final Macros WHEY = Macros.fromGrams(24, 3, 1.5);        // ~1 scoop
    private static final Macros MASS_GAINER = new Macros(650, 50, 85, 8);   // ~1 large scoop
    private static final Macros OLIVE_OIL_TBSP = new Macros(119, 0, 0, 13.5);
    private static final Macros PEANUT_BUTTER_TBSP = new Macros(94, 4, 3, 8);
    private static final Macros OATS_HALF_CUP = new Macros(150, 5, 27, 3);

    /** Thresholds below which a gap isn't worth supplementing. */
    private static final double PROTEIN_GAP_MIN = 15;   // grams
    private static final double CALORIE_GAP_MIN = 150;  // kcal

    public List<SupplementSuggestion> suggest(Macros adjustment) {
        List<SupplementSuggestion> out = new ArrayList<>();

        double calGap = adjustment.calories();
        double proteinGap = adjustment.protein();
        double carbGap = adjustment.carbs();

        // --- Need MORE intake (bulk / recomp shortfall) ---
        if (proteinGap >= PROTEIN_GAP_MIN) {
            int scoops = clamp((int) Math.ceil(proteinGap / WHEY.protein()), 1, 4);
            out.add(new SupplementSuggestion(
                    "Whey protein",
                    String.format("You're ~%.0fg short on protein. A shake is the easiest way to close it.", proteinGap),
                    "~1 scoop in water or milk",
                    WHEY,
                    scoops));
        }

        if (calGap >= 500) {
            out.add(new SupplementSuggestion(
                    "Mass gainer",
                    String.format("Big calorie surplus to hit (~%.0f kcal). A gainer shake saves you from force-feeding.", calGap),
                    "~1 large scoop blended with milk",
                    MASS_GAINER,
                    1));
        } else if (calGap >= CALORIE_GAP_MIN) {
            int tbsp = clamp((int) Math.ceil(calGap / OLIVE_OIL_TBSP.calories()), 1, 4);
            out.add(new SupplementSuggestion(
                    "Olive oil (calorie-dense add-on)",
                    String.format("Add ~%.0f kcal without feeling full — drizzle over existing meals.", calGap),
                    "1 tbsp = ~119 kcal",
                    OLIVE_OIL_TBSP,
                    tbsp));
            out.add(new SupplementSuggestion(
                    "Peanut butter",
                    "Tasty, calorie-dense, and adds a little protein.",
                    "1 tbsp = ~94 kcal",
                    PEANUT_BUTTER_TBSP,
                    2));
        }

        if (carbGap >= 60 && calGap > 0) {
            int servings = clamp((int) Math.ceil(carbGap / OATS_HALF_CUP.carbs()), 1, 3);
            out.add(new SupplementSuggestion(
                    "Oats (easy carbs)",
                    String.format("~%.0fg carbs short — oats blend into shakes for cheap, clean energy.", carbGap),
                    "1/2 cup dry = ~27g carbs",
                    OATS_HALF_CUP,
                    servings));
        }

        // --- Need LESS intake (cut surplus) ---
        if (calGap <= -CALORIE_GAP_MIN) {
            out.add(new SupplementSuggestion(
                    "Whey + psyllium fiber (satiety swap)",
                    String.format("You're ~%.0f kcal over target. Swap a calorie-dense snack for a protein shake + fiber to stay full on fewer calories.",
                            Math.abs(calGap)),
                    "1 scoop whey + 1 tsp psyllium husk",
                    WHEY,
                    1));
        }

        return out;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
