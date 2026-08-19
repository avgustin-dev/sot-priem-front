package kg.sot.reception.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Явный бин ObjectMapper: используется Spring MVC для тела всех ответов и внедряется
 * в RestAuthenticationEntryPoint / RestAccessDeniedHandler. Не полагаемся на то, что
 * автоконфигурация Jackson соберёт его сама — Spring Boot использует именно этот бин
 * при построении HTTP-конвертера, если он определён явно.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}
