package co.fittrack.dto;

import co.fittrack.model.Weekday;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Copy one day's logged meals onto one or more other days — supports the
 * "same meals for multiple days" flow (e.g. copy Monday → Tue/Wed/Thu).
 */
public record CopyDayRequest(
        @NotNull Weekday sourceWeekday,
        @NotEmpty List<Weekday> targetWeekdays) {
}
