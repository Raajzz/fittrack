package co.fittrack.controller;

import co.fittrack.dto.ProfileRequest;
import co.fittrack.model.UserProfile;
import co.fittrack.repository.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** CRUD for the user's profile (stats + goal). */
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final UserProfileRepository profiles;

    public ProfileController(UserProfileRepository profiles) {
        this.profiles = profiles;
    }

    @PostMapping
    public ResponseEntity<UserProfile> create(@Valid @RequestBody ProfileRequest req) {
        UserProfile saved = profiles.save(apply(new UserProfile(), req));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public UserProfile get(@PathVariable Long id) {
        return profiles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @PutMapping("/{id}")
    public UserProfile update(@PathVariable Long id, @Valid @RequestBody ProfileRequest req) {
        UserProfile existing = profiles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return profiles.save(apply(existing, req));
    }

    private UserProfile apply(UserProfile p, ProfileRequest req) {
        p.setName(req.name());
        p.setSex(req.sex());
        p.setAge(req.age());
        p.setHeightCm(req.heightCm());
        p.setWeightKg(req.weightKg());
        p.setActivityLevel(req.activityLevel());
        p.setGoal(req.goal());
        return p;
    }
}
