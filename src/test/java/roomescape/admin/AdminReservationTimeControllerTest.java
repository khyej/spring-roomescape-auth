package roomescape.admin;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthService;
import roomescape.config.AuthArgumentResolver;
import roomescape.exception.AlreadyInUseException;
import roomescape.time.ReservationTimeService;
import roomescape.user.User;

@WebMvcTest(AdminReservationTimeController.class)
@Import({AuthArgumentResolver.class})
class AdminReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private AuthService authService;

    private static final String TOKEN = "Bearer test-token";
    private static final User TEST_USER = new User(1L, "관리자", "admin", "admin1234");

    private void givenAuthenticated() {
        given(authService.authenticate("test-token")).willReturn(TEST_USER);
    }

    @Test
    void 예약시간_추가() throws Exception {
        givenAuthenticated();
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "20:00");

        mockMvc.perform(post("/api/admin/times")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isCreated());
    }

    @Test
    void 예약시간_삭제() throws Exception {
        givenAuthenticated();
        willDoNothing()
                .given(reservationTimeService).delete(1L);

        mockMvc.perform(delete("/api/admin/times/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void 빈값으로_예약시간_추가시_400() throws Exception {
        givenAuthenticated();
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", null);

        mockMvc.perform(post("/api/admin/times")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 사용_중인_예약시간_삭제시_409() throws Exception {
        givenAuthenticated();
        willThrow(new AlreadyInUseException("예약 시간에 해당하는 예약이 있습니다."))
                .given(reservationTimeService).delete(1L);

        mockMvc.perform(delete("/api/admin/times/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isConflict());
    }

    @Test
    void 토큰_없이_예약시간_추가시_401() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "20:00");

        mockMvc.perform(post("/api/admin/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 토큰_없이_예약시간_삭제시_401() throws Exception {
        mockMvc.perform(delete("/api/admin/times/1"))
                .andExpect(status().isUnauthorized());
    }
}
