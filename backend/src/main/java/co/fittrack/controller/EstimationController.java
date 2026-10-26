package co.fittrack.controller;

import co.fittrack.dto.EstimateRequest;
import co.fittrack.dto.EstimationResult;
import co.fittrack.service.MealEstimationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI estimation of a photo meal's macros. */
@RestController
@RequestMapping("/api/meals")
public class EstimationController {

    private final MealEstimationService estimationService;

    public EstimationController(MealEstimationService estimationService) {
        this.estimationService = estimationService;
    }

    /**
     * Stateless estimate — no saved meal required. Send a base64 image and/or a
     * description; get back estimated macros to pre-fill the form before logging.
     */
    @PostMapping("/estimate")
    public EstimationResult preview(@RequestBody EstimateRequest request) {
        return estimationService.preview(request);
    }

    /**
     * Estimate macros for an already-saved meal and write them back onto it. Send an
     * optional {@link EstimateRequest} body with inline base64 image bytes (for device
     * photos) — or omit it to use the meal's stored photo URL / description.
     */
    @PostMapping("/{mealId}/estimate")
    public EstimationResult estimate(@PathVariable Long mealId,
                                     @RequestBody(required = false) EstimateRequest request) {
        return estimationService.estimate(mealId, request);
    }
}
