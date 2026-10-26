package co.fittrack.dto;

/**
 * Body for the estimate endpoints. Because device photo URIs (file://…) aren't
 * reachable from the server, the client sends the image bytes inline as base64.
 *
 * @param imageBase64 raw base64 image data (no data: prefix), or null.
 * @param mediaType   one of image/jpeg, image/png, image/gif, image/webp.
 * @param description optional text description to refine the estimate.
 * @param photoUrl    optional public image URL the model can fetch (preview endpoint).
 */
public record EstimateRequest(String imageBase64, String mediaType, String description, String photoUrl) {
}
