package roomescape.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void add(String token, Instant expiry) {
        blacklistedTokens.put(token, expiry);
    }

    public boolean contains(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Scheduled(fixedRate = 3_600_000)
    public void evictExpired() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
