package kg.sot.reception.service;

import kg.sot.reception.dto.LoginRequest;
import kg.sot.reception.dto.LoginResponse;
import kg.sot.reception.dto.StaffProfile;
import kg.sot.reception.exception.ApiException;
import kg.sot.reception.model.StaffUser;
import kg.sot.reception.repository.StaffUserRepository;
import kg.sot.reception.security.JwtService;
import kg.sot.reception.util.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StaffUserRepository staffUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        StaffUser user = staffUserRepository.findByLoginIgnoreCase(req.login().trim())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Неверный логин или пароль"));
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, Mappers.toStaffProfile(user));
    }

    @Transactional(readOnly = true)
    public StaffProfile me(StaffUser user) {
        return Mappers.toStaffProfile(user);
    }
}
