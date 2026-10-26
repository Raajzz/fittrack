package co.fittrack.dto;

import co.fittrack.model.ActivityLevel;
import co.fittrack.model.Goal;
import co.fittrack.model.Sex;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Payload to create or update a {@link co.fittrack.model.UserProfile}. */
public record ProfileRequest(
        String name,
        @NotNull Sex sex,
        @Min(10) @Max(100) int age,
        @Positive double heightCm,
        @Positive double weightKg,
        @NotNull ActivityLevel activityLevel,
        @NotNull Goal goal) {
}
