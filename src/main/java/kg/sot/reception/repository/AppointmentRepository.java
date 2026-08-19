package kg.sot.reception.repository;

import kg.sot.reception.model.Appointment;
import kg.sot.reception.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    Optional<Appointment> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            select a from Appointment a
            where a.date = :date and a.targetId = :targetId
            and a.status not in ('cancelled', 'rejected')
            """)
    List<Appointment> findActiveByDateAndTarget(@Param("date") LocalDate date, @Param("targetId") String targetId);

    @Query("""
            select a from Appointment a
            where (:status is null or a.status = :status)
            and (:date is null or a.date = :date)
            and (:targetId is null or a.targetId = :targetId)
            order by a.createdAt desc
            """)
    List<Appointment> search(
            @Param("status") AppointmentStatus status,
            @Param("date") LocalDate date,
            @Param("targetId") String targetId
    );

    List<Appointment> findAllByOrderByCreatedAtDesc();
}
