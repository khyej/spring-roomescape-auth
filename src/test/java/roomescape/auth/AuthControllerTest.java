package roomescape.auth;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.config.AuthArgumentResolver;
import roomescape.exception.AuthenticationException;
import roomescape.user.User;

@WebMvcTest(AuthController.class)
@Import({AuthArgumentResolver.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private static final String TOKEN = "test-token";
    private static final String BEARER_TOKEN = "Bearer " + TOKEN;
    private static final User TEST_USER = new User(1L, "동키", "donkey", "password1", roomescape.user.Role.USER, null);

    @Test
    void 로그인_성공시_토큰_반환() throws Exception {
        given(authService.login(new LoginRequest("donkey", "password1")))
                .willReturn(new LoginResponse(TOKEN));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "donkey", "password", "password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN));
    }

    @Test
    void 잘못된_비밀번호로_로그인시_401() throws Exception {
        given(authService.login(new LoginRequest("donkey", "wrong")))
                .willThrow(new AuthenticationException("아이디 또는 비밀번호가 올바르지 않습니다."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "donkey", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그아웃_성공시_204() throws Exception {
        given(authService.authenticate(TOKEN)).willReturn(TEST_USER);
        willDoNothing().given(authService).logout(TEST_USER.getId());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void 토큰_없이_로그아웃시_401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 다른_기기_로그인으로_무효화된_토큰으로_요청시_401() throws Exception {
        given(authService.authenticate(TOKEN))
                .willThrow(new AuthenticationException("다른 기기에서 로그인했거나 로그아웃된 토큰입니다."));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", BEARER_TOKEN))
                .andExpect(status().isUnauthorized());
    }
}
