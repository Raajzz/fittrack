package co.fittrack.controller;

import co.fittrack.dto.CopyDayRequest;
import co.fittrack.dto.DietDayRequest;
import co.fittrack.model.DietDay;
import co.fittrack.service.DietService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Weekly diet logging for a user (Monday → Sunday). */
@RestController
@RequestMapping("/api/users/{userId}/diet")
public class DietController {

    private final DietService dietService;

    public DietController(DietService dietService) {
        this.dietService = dietService;
    }

    /** The full logged week. */
    @GetMapping
    public List<DietDay> week(@PathVariable Long userId) {
        return dietService.week(userId);
    }

    /** Create or replace the meals for one weekday. */
    @PutMapping("/day")
    public DietDay logDay(@PathVariable Long userId, @Valid @RequestBody DietDayRequest req) {
        return dietService.logDay(userId, req);
    }

    /** Copy one day's meals onto multiple other days. */
    @PostMapping("/copy")
    public List<DietDay> copy(@PathVariable Long userId, @Valid @RequestBody CopyDayRequest req) {
        return dietService.copyDay(userId, req.sourceWeekday(), req.targetWeekdays());
    }
}
