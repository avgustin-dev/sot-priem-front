package kg.sot.reception.controller;

import jakarta.validation.Valid;
import kg.sot.reception.dto.LoginRequest;
import kg.sot.reception.dto.LoginResponse;
import kg.sot.reception.dto.StaffProfile;
import kg.sot.reception.security.StaffPrincipal;
import kg.sot.reception.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public StaffProfile me(@AuthenticationPrincipal StaffPrincipal principal) {
        return authService.me(principal.getUser());
    }
}
