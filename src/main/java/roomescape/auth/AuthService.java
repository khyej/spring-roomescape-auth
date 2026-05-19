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

    public AuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
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
        Long userId = tokenProvider.extract(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("존재하지 않는 사용자입니다."));
    }
}
