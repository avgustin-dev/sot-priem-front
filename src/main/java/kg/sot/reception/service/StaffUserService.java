package kg.sot.reception.service;

import kg.sot.reception.dto.StaffProfile;
import kg.sot.reception.repository.StaffUserRepository;
import kg.sot.reception.util.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StaffUserService {

    private final StaffUserRepository staffUserRepository;

    public StaffUserService(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffProfile> list() {
        return staffUserRepository.findAllByOrderByFullNameAsc().stream()
                .map(Mappers::toStaffProfile)
                .toList();
    }
}
