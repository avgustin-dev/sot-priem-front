package kg.sot.reception.service;

import kg.sot.reception.dto.LoginRequest;
import kg.sot.reception.dto.LoginResponse;
import kg.sot.reception.dto.StaffProfile;
import kg.sot.reception.exception.ApiException;
import kg.sot.reception.model.StaffUser;
import kg.sot.reception.repository.StaffUserRepository;
import kg.sot.reception.security.JwtService;
import kg.sot.reception.util.Mappers;
import kg.sot.reception.util.RateLimiter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);

    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimiter rateLimiter;

    public AuthService(
            StaffUserRepository staffUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RateLimiter rateLimiter
    ) {
        this.staffUserRepository = staffUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String loginKey = "login:" + req.login().trim().toLowerCase();
        if (!rateLimiter.tryConsume(loginKey, MAX_LOGIN_ATTEMPTS, LOGIN_WINDOW)) {
            throw ApiException.rateLimited("Слишком много попыток входа. Повторите позже.");
        }
        StaffUser user = staffUserRepository.findByLoginIgnoreCase(req.login().trim())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Неверный логин или пароль"));
        rateLimiter.reset(loginKey);
        String token = jwtService.generateToken(user);
        return new LoginResponse(token, Mappers.toStaffProfile(user));
    }

    @Transactional(readOnly = true)
    public StaffProfile me(StaffUser user) {
        return Mappers.toStaffProfile(user);
    }
}
