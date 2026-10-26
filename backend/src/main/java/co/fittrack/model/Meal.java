package co.fittrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single logged meal within a day.
 *
 * <p>For {@link MealInputType#TEXT} meals the user supplies the description and
 * (optionally) the macros. For {@link MealInputType#PHOTO} meals, {@code photoUrl}
 * points at the uploaded image and the macro fields stay null until estimated.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MealInputType inputType = MealInputType.TEXT;

    /** Free-text description, e.g. "200g chicken breast, 1 cup rice". */
    private String description;

    /** Set when inputType == PHOTO. */
    private String photoUrl;

    // Nullable: unknown until entered or estimated (esp. for photos).
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;

    /** True once macros are confidently known (user-entered or AI-estimated). */
    private boolean estimated = true;

    public Macros toMacros() {
        return new Macros(
                calories == null ? 0 : calories,
                protein == null ? 0 : protein,
                carbs == null ? 0 : carbs,
                fat == null ? 0 : fat);
    }
}
