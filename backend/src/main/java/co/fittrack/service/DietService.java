package co.fittrack.service;

import co.fittrack.dto.DietDayRequest;
import co.fittrack.dto.MealInput;
import co.fittrack.model.DietDay;
import co.fittrack.model.Meal;
import co.fittrack.model.MealInputType;
import co.fittrack.model.Weekday;
import co.fittrack.repository.DietDayRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logging and managing a user's weekly diet (Monday → Sunday). */
@Service
public class DietService {

    private final DietDayRepository dietDays;

    public DietService(DietDayRepository dietDays) {
        this.dietDays = dietDays;
    }

    public List<DietDay> week(Long userId) {
        return dietDays.findByUserId(userId);
    }

    /** Create or fully replace the meals logged for one weekday. */
    @Transactional
    public DietDay logDay(Long userId, DietDayRequest request) {
        DietDay day = dietDays.findByUserIdAndWeekday(userId, request.weekday())
                .orElseGet(() -> {
                    DietDay d = new DietDay();
                    d.setUserId(userId);
                    d.setWeekday(request.weekday());
                    return d;
                });

        day.getMeals().clear();
        if (request.meals() != null) {
            for (MealInput input : request.meals()) {
                day.getMeals().add(toMeal(input));
            }
        }
        return dietDays.save(day);
    }

    /**
     * Copy the source day's meals onto each target weekday. Supports
     * "same description for multiple days". Existing meals on a target day are replaced.
     */
    @Transactional
    public List<DietDay> copyDay(Long userId, Weekday source, List<Weekday> targets) {
        DietDay src = dietDays.findByUserIdAndWeekday(userId, source)
                .orElseThrow(() -> new IllegalArgumentException("No meals logged for " + source));

        List<DietDay> saved = new ArrayList<>();
        for (Weekday target : targets) {
            if (target == source) {
                continue;
            }
            DietDay day = dietDays.findByUserIdAndWeekday(userId, target)
                    .orElseGet(() -> {
                        DietDay d = new DietDay();
                        d.setUserId(userId);
                        d.setWeekday(target);
                        return d;
                    });
            day.getMeals().clear();
            for (Meal m : src.getMeals()) {
                day.getMeals().add(copyMeal(m));
            }
            saved.add(dietDays.save(day));
        }
        return saved;
    }

    private Meal toMeal(MealInput input) {
        Meal m = new Meal();
        m.setInputType(input.inputType() == null ? MealInputType.TEXT : input.inputType());
        m.setDescription(input.description());
        m.setPhotoUrl(input.photoUrl());
        m.setCalories(input.calories());
        m.setProtein(input.protein());
        m.setCarbs(input.carbs());
        m.setFat(input.fat());
        // A photo with no macros yet is "not estimated"; everything else is treated as known.
        boolean missingMacros = input.calories() == null && input.protein() == null;
        m.setEstimated(!(m.getInputType() == MealInputType.PHOTO && missingMacros));
        return m;
    }

    private Meal copyMeal(Meal src) {
        Meal m = new Meal();
        m.setInputType(src.getInputType());
        m.setDescription(src.getDescription());
        m.setPhotoUrl(src.getPhotoUrl());
        m.setCalories(src.getCalories());
        m.setProtein(src.getProtein());
        m.setCarbs(src.getCarbs());
        m.setFat(src.getFat());
        m.setEstimated(src.isEstimated());
        return m;
    }
}
