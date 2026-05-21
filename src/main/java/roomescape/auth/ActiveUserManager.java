package roomescape.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ActiveUserManager {

    private final Map<Long, String> activeTokens = new ConcurrentHashMap<>();

    public void register(Long userId, String token) {
        activeTokens.put(userId, token);
    }

    public boolean isValid(Long userId, String token) {
        String latestToken = activeTokens.get(userId);
        return token.equals(latestToken);
    }

    public void remove(Long userId) {
        activeTokens.remove(userId);
    }
}
