package co.fittrack.model;

/**
 * How the user described a meal.
 *
 * <ul>
 *   <li>{@code TEXT} – the user typed the food and amounts; macros are known / entered.</li>
 *   <li>{@code PHOTO} – the user uploaded a picture. Macros may be null until an
 *       estimation step (manual or AI vision) fills them in.</li>
 * </ul>
 */
public enum MealInputType {
    TEXT,
    PHOTO
}
