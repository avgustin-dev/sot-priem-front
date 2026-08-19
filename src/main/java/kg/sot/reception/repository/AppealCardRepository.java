package kg.sot.reception.repository;

import kg.sot.reception.model.AppealCard;
import kg.sot.reception.model.AppealStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppealCardRepository extends JpaRepository<AppealCard, String> {

    Optional<AppealCard> findByAppointmentId(String appointmentId);

    Optional<AppealCard> findByCodeIgnoreCase(String code);

    List<AppealCard> findByStageOrderByCreatedAtDesc(AppealStage stage);

    List<AppealCard> findAllByOrderByCreatedAtDesc();
}
