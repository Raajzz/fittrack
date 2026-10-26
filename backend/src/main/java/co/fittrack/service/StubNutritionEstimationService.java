package co.fittrack.service;

import co.fittrack.dto.EstimationResult;

/**
 * Fallback used when no Anthropic API key is configured. It never guesses macros —
 * it returns a not-confident result telling the user to enter macros manually.
 * This keeps the app fully functional without an AI key.
 */
public class StubNutritionEstimationService implements NutritionEstimationService {

    @Override
    public EstimationResult estimate(String imageBase64, String mediaType, String photoUrl, String description) {
        return EstimationResult.unavailable(
                "AI estimation is not configured. Set ANTHROPIC_API_KEY to enable photo estimation, "
                        + "or enter this meal's calories and macros manually.");
    }
}
