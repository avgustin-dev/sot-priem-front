package kg.sot.reception.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.sot.reception.security.StaffPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Одна строка в журнал AUDIT на каждый HTTP-запрос к API: кто, что, когда, с каким
 * результатом (включая отказы 401/403). Тела запросов/ответов не логируются —
 * там могут быть PIN и пароли, в audit-лог попадают только метаданные.
 *
 * Зарегистрирован в SecurityConfig через addFilterBefore(..., ExceptionTranslationFilter.class),
 * а не как обычный @Component-фильтр: так он остаётся внутри цепочки Spring Security —
 * видит и итоговую аутентификацию (проставленную JwtAuthenticationFilter), и финальный
 * статус ответа, даже если AuthorizationFilter отклонил запрос до контроллера.
 */
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            audit.info("actor={} ip={} method={} path={}{} status={} durationMs={}",
                    resolveActor(), resolveClientIp(request), method, path,
                    query != null ? "?" + query : "",
                    response.getStatus(), durationMs);
        }
    }

    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof StaffPrincipal principal) {
            return principal.getUser().getLogin() + "(" + principal.getUser().getRole() + ")";
        }
        return "public";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator/health");
    }
}
