package co.fittrack.service;

import co.fittrack.dto.EstimationResult;
import co.fittrack.model.Macros;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlockParam;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Estimates meal macros using Claude's vision model (Opus 4.8) with structured
 * output — the model is forced to return a validated {@link EstimatedMacros} object,
 * so there's no brittle text parsing.
 *
 * <p>Accepts either inline base64 image bytes (how the mobile client sends a device
 * photo) or a public image URL the model can fetch. Falls back to a description-only
 * estimate when no image is available.
 */
public class ClaudeNutritionEstimationService implements NutritionEstimationService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeNutritionEstimationService.class);

    /** Passed as a string so the build doesn't depend on an SDK-version-specific Model constant. */
    private static final String MODEL = "claude-opus-4-8";

    private static final String PROMPT = """
            You are a nutrition estimator. Estimate the nutrition for the meal shown and/or described.
            Infer typical portion sizes from the image. If a text description is provided, use it to
            refine the estimate. Return your best single estimate of the meal's total calories (kcal)
            and grams of protein, carbs, and fat, plus a one-sentence note on what you assumed.
            If you genuinely cannot tell what the food is, return zeros and say so in the note.
            """;

    private final AnthropicClient client;

    public ClaudeNutritionEstimationService(String apiKey) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }

    /** Structured schema the model must fill — Jackson descriptions guide the model. */
    public record EstimatedMacros(
            @JsonPropertyDescription("Total calories for the whole meal, in kcal") double calories,
            @JsonPropertyDescription("Total protein in grams") double protein,
            @JsonPropertyDescription("Total carbohydrates in grams") double carbs,
            @JsonPropertyDescription("Total fat in grams") double fat,
            @JsonPropertyDescription("One sentence on what was assumed, or why it couldn't be estimated") String note) {
    }

    @Override
    public EstimationResult estimate(String imageBase64, String mediaType, String photoUrl, String description) {
        List<ContentBlockParam> blocks = new ArrayList<>();

        if (imageBase64 != null && !imageBase64.isBlank()) {
            blocks.add(ContentBlockParam.ofImage(ImageBlockParam.builder()
                    .source(Base64ImageSource.builder()
                            .data(imageBase64)
                            .mediaType(toMediaType(mediaType))
                            .build())
                    .build()));
        } else if (isFetchableUrl(photoUrl)) {
            blocks.add(ContentBlockParam.ofImage(ImageBlockParam.builder()
                    .urlSource(photoUrl)
                    .build()));
        }

        StringBuilder text = new StringBuilder(PROMPT);
        if (description != null && !description.isBlank()) {
            text.append("\n\nUser's description of the meal: ").append(description);
        }
        if (blocks.isEmpty() && (description == null || description.isBlank())) {
            return EstimationResult.unavailable("No photo or description to estimate from.");
        }
        blocks.add(ContentBlockParam.ofText(TextBlockParam.builder().text(text.toString()).build()));

        try {
            var params = MessageCreateParams.builder()
                    .model(MODEL)
                    .maxTokens(1024L)
                    .addUserMessageOfBlockParams(blocks)
                    .outputConfig(EstimatedMacros.class)
                    .build();

            EstimatedMacros est = client.messages().create(params).content().stream()
                    .flatMap(cb -> cb.text().stream())
                    .findFirst()
                    .map(t -> t.text())
                    .orElse(null);

            if (est == null) {
                return EstimationResult.unavailable("The model returned no estimate.");
            }
            Macros macros = new Macros(est.calories(), est.protein(), est.carbs(), est.fat()).rounded();
            boolean confident = macros.calories() > 0;
            return new EstimationResult(macros, confident,
                    est.note() == null ? "Estimated from the meal photo." : est.note());
        } catch (Exception e) {
            log.warn("Nutrition estimation failed", e);
            return EstimationResult.unavailable("Estimation failed: " + e.getMessage());
        }
    }

    private static boolean isFetchableUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private static Base64ImageSource.MediaType toMediaType(String mediaType) {
        if (mediaType == null) {
            return Base64ImageSource.MediaType.IMAGE_JPEG;
        }
        return switch (mediaType.toLowerCase()) {
            case "image/png" -> Base64ImageSource.MediaType.IMAGE_PNG;
            case "image/gif" -> Base64ImageSource.MediaType.IMAGE_GIF;
            case "image/webp" -> Base64ImageSource.MediaType.IMAGE_WEBP;
            default -> Base64ImageSource.MediaType.IMAGE_JPEG;
        };
    }
}
