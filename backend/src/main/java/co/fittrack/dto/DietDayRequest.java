package co.fittrack.dto;

import co.fittrack.model.Weekday;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Log (or replace) the meals for a single weekday. */
public record DietDayRequest(
        @NotNull Weekday weekday,
        List<MealInput> meals) {
}
