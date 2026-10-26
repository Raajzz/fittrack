package co.fittrack.controller;

import co.fittrack.dto.PlanResponse;
import co.fittrack.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The crafted meal plan (targets, adjustments, supplement alternatives). */
@RestController
@RequestMapping("/api/users/{userId}/plan")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public PlanResponse plan(@PathVariable Long userId) {
        return planService.buildPlan(userId);
    }
}
