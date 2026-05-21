package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.AuthenticationException;
import roomescape.user.User;
import roomescape.user.Role;
import roomescape.user.repository.UserRepository;

@SpringBootTest
@Transactional
class SimultaneousLoginTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private final String username = "donkey";
    private final String password = "password1";

    @Test
    void 새로운_로그인이_발생하면_기존_토큰은_무효화된다() {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String firstToken = authService.login(loginRequest).token();

        String secondToken = authService.login(loginRequest).token();

        assertThat(firstToken).isNotEqualTo(secondToken);

        User user = authService.authenticate(secondToken);
        assertThat(user.getUsername()).isEqualTo(username);

        assertThatThrownBy(() -> authService.authenticate(firstToken))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("다른 기기에서 로그인했거나 로그아웃된 토큰입니다.");
    }
}
