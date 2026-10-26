package co.fittrack.dto;

import co.fittrack.model.MealInputType;

/**
 * One meal as sent by the client. For TEXT meals the macro fields are typically
 * filled in; for PHOTO meals {@code photoUrl} is set and macros may be null
 * (to be estimated later).
 */
public record MealInput(
        MealInputType inputType,
        String description,
        String photoUrl,
        Double calories,
        Double protein,
        Double carbs,
        Double fat) {
}
