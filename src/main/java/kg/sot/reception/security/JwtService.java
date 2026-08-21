package kg.sot.reception.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kg.sot.reception.config.JwtProperties;
import kg.sot.reception.model.StaffUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String DEFAULT_SECRET_PREFIX = "change-me-development-only";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        if (properties.secret().startsWith(DEFAULT_SECRET_PREFIX)) {
            log.warn("!!! APP_JWT_SECRET не задан — используется секрет по умолчанию из application.yml. "
                    + "Любой, кто видел этот репозиторий, может подделать JWT сотрудника. "
                    + "Обязательно задайте APP_JWT_SECRET перед выходом за пределы localhost. !!!");
        }
    }

    public String generateToken(StaffUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.expirationMinutes() * 60);
        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId())
                .claim("role", user.getRole().name())
                .claim("login", user.getLogin())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
