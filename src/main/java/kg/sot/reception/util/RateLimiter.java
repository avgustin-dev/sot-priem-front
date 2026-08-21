package kg.sot.reception.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Простой in-memory лимитер попыток (фиксированное окно) по произвольному ключу —
 * защищает от перебора PIN / пароля. Рассчитан на один инстанс приложения;
 * при горизонтальном масштабировании потребуется вынести в Redis.
 */
@Component
public class RateLimiter {

    private record Bucket(AtomicInteger count, Instant windowStart) {
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /** true — попытка разрешена и засчитана; false — лимит исчерпан. */
    public boolean tryConsume(String key, int maxAttempts, Duration window) {
        Instant now = Instant.now();
        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart().plus(window).isBefore(now)) {
                return new Bucket(new AtomicInteger(0), now);
            }
            return existing;
        });
        return bucket.count().incrementAndGet() <= maxAttempts;
    }

    public void reset(String key) {
        buckets.remove(key);
    }
}
