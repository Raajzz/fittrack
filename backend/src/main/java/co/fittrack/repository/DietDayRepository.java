package co.fittrack.repository;

import co.fittrack.model.DietDay;
import co.fittrack.model.Weekday;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietDayRepository extends JpaRepository<DietDay, Long> {

    List<DietDay> findByUserId(Long userId);

    Optional<DietDay> findByUserIdAndWeekday(Long userId, Weekday weekday);
}
