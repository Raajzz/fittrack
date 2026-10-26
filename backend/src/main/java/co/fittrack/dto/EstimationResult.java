package co.fittrack.dto;

import co.fittrack.model.Macros;

/**
 * The outcome of estimating a meal's macros from a photo (and/or description).
 *
 * @param macros    the estimated per-meal macros.
 * @param confident true when a real estimate was produced; false when the
 *                  estimator was unavailable or couldn't read the input.
 * @param note      short human-readable explanation (what the model saw, or why it couldn't).
 */
public record EstimationResult(Macros macros, boolean confident, String note) {

    public static EstimationResult unavailable(String note) {
        return new EstimationResult(Macros.ZERO, false, note);
    }
}
