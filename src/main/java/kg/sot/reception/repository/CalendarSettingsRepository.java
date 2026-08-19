package kg.sot.reception.repository;

import kg.sot.reception.model.CalendarSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarSettingsRepository extends JpaRepository<CalendarSettings, Integer> {
}
