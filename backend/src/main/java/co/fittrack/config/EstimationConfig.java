package co.fittrack.config;

import co.fittrack.service.ClaudeNutritionEstimationService;
import co.fittrack.service.NutritionEstimationService;
import co.fittrack.service.StubNutritionEstimationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the nutrition estimator: the Claude vision implementation when an API key
 * is configured, otherwise a stub that asks the user to enter macros manually.
 * The app runs either way — AI estimation is an optional enhancement.
 */
@Configuration
public class EstimationConfig {

    private static final Logger log = LoggerFactory.getLogger(EstimationConfig.class);

    @Bean
    public NutritionEstimationService nutritionEstimationService(
            @Value("${app.anthropic.api-key:}") String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            log.info("Anthropic API key found — enabling Claude photo estimation.");
            return new ClaudeNutritionEstimationService(apiKey);
        }
        log.info("No Anthropic API key — photo estimation disabled (manual macro entry only).");
        return new StubNutritionEstimationService();
    }
}
