package co.fittrack.service;

import co.fittrack.dto.EstimationResult;

/**
 * Estimates a meal's macros from a photo and/or text description.
 *
 * <p>Pluggable on purpose: the default implementation calls Claude's vision model,
 * but any implementation (a different provider, a local model, a manual-entry stub)
 * can be swapped in without touching callers.
 */
public interface NutritionEstimationService {

    /**
     * @param imageBase64  base64 image bytes, or null.
     * @param mediaType    media type for the base64 image (e.g. image/jpeg), or null.
     * @param photoUrl     a public image URL the model can fetch, or null.
     * @param description  user's text description of the meal, or null.
     */
    EstimationResult estimate(String imageBase64, String mediaType, String photoUrl, String description);
}
