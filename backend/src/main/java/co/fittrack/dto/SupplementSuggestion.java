package co.fittrack.dto;

import co.fittrack.model.Macros;

/**
 * A concrete "if you can't eat that much from food" alternative — e.g. a whey
 * shake to close a protein gap, or olive oil / mass gainer to close a calorie gap.
 */
public record SupplementSuggestion(
        String name,
        String reason,
        String servingInfo,
        Macros perServing,
        int suggestedServings) {
}
