package kg.sot.reception.security;

import kg.sot.reception.model.StaffUser;
import kg.sot.reception.repository.StaffUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffUserRepository staffUserRepository;

    public StaffUserDetailsService(StaffUserRepository staffUserRepository) {
        this.staffUserRepository = staffUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        StaffUser user = staffUserRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new UsernameNotFoundException("Неверный логин или пароль"));
        return new StaffPrincipal(user);
    }

    public UserDetails loadUserById(String id) {
        StaffUser user = staffUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new StaffPrincipal(user);
    }
}
