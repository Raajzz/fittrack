package co.fittrack.service;

import co.fittrack.dto.EstimateRequest;
import co.fittrack.dto.EstimationResult;
import co.fittrack.model.Macros;
import co.fittrack.model.Meal;
import co.fittrack.repository.MealRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Estimates a stored meal's macros (typically a photo meal with unknown macros),
 * writes the result back onto the meal, and marks it estimated so it starts
 * counting toward the daily totals and the plan.
 */
@Service
public class MealEstimationService {

    private final MealRepository meals;
    private final NutritionEstimationService estimator;

    public MealEstimationService(MealRepository meals, NutritionEstimationService estimator) {
        this.meals = meals;
        this.estimator = estimator;
    }

    /** Stateless estimate — no persisted meal required. Lets the client estimate a
     *  photo before logging it and pre-fill the macro fields. */
    public EstimationResult preview(EstimateRequest request) {
        if (request == null) {
            return EstimationResult.unavailable("No image or description provided.");
        }
        return estimator.estimate(
                request.imageBase64(), request.mediaType(), request.photoUrl(), request.description());
    }

    @Transactional
    public EstimationResult estimate(Long mealId, EstimateRequest request) {
        Meal meal = meals.findById(mealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal not found"));

        String base64 = request == null ? null : request.imageBase64();
        String mediaType = request == null ? null : request.mediaType();

        EstimationResult result = estimator.estimate(base64, mediaType, meal.getPhotoUrl(), meal.getDescription());

        if (result.confident()) {
            Macros m = result.macros();
            meal.setCalories(m.calories());
            meal.setProtein(m.protein());
            meal.setCarbs(m.carbs());
            meal.setFat(m.fat());
            meal.setEstimated(true);
            meals.save(meal);
        }
        return result;
    }
}
