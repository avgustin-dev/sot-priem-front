package kg.sot.reception.repository;

import kg.sot.reception.model.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffUserRepository extends JpaRepository<StaffUser, String> {

    Optional<StaffUser> findByLoginIgnoreCase(String login);

    List<StaffUser> findAllByOrderByFullNameAsc();
}
