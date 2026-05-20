package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import roomescape.exception.AuthenticationException;

public class BearerTokenExtractor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private BearerTokenExtractor() {
    }

    public static String extract(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException("인증 토큰이 없습니다.");
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
