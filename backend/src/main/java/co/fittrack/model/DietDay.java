package co.fittrack.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A single weekday's worth of logged meals for one user. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DietDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private Weekday weekday;

    // Eager: a day has only a few meals, and both the plan service and the
    // week endpoint's JSON serialization read them outside an open session
    // (open-in-view is disabled). Eager fetch avoids LazyInitializationException.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Meal> meals = new ArrayList<>();

    /** Sum of every meal's macros for the day. Photo meals with unknown macros contribute 0. */
    public Macros totalMacros() {
        Macros total = Macros.ZERO;
        for (Meal meal : meals) {
            total = total.plus(meal.toMacros());
        }
        return total;
    }
}
