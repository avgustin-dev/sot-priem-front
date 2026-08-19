package kg.sot.reception.repository;

import kg.sot.reception.model.SurveyResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, String> {

    List<SurveyResponseEntity> findAllByOrderBySubmittedAtDesc();
}
