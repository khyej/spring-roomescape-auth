package roomescape.auth;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.exception.AuthenticationException;
import roomescape.user.User;
import roomescape.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(UserRepository userRepository, TokenProvider tokenProvider, TokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.tokenBlacklist = tokenBlacklist;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new AuthenticationException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!user.getPassword().equals(loginRequest.password())) {
            throw new AuthenticationException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = tokenProvider.create(user.getId());
        return new LoginResponse(token);
    }

    public User authenticate(String token) {
        if (tokenBlacklist.contains(token)) {
            throw new AuthenticationException("로그아웃된 토큰입니다.");
        }
        Long userId = tokenProvider.extract(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 사용자입니다."));
    }

    public void logout(String token) {
        tokenBlacklist.add(token, tokenProvider.getExpiration(token));
    }
}
