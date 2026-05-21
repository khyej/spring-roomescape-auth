package roomescape.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
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
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidStateException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.dto.PageReservationsResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationsResponse;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.user.User;

@WebMvcTest(ReservationController.class)
@Import({AuthArgumentResolver.class})
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AuthService authService;

    private static final String TOKEN = "Bearer test-token";
    private static final User TEST_USER = new User(1L, "동키", "donkey", "password1", roomescape.user.Role.USER, null);

    private void givenAuthenticated() {
        given(authService.authenticate("test-token")).willReturn(TEST_USER);
    }

    @Test
    void 예약_조회() throws Exception {
        givenAuthenticated();
        given(reservationService.read(any(), any(Integer.class), any(Integer.class)))
                .willReturn(PageReservationsResponse.from(List.of(), 0, 0, false));

        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void 사용자_예약_조회() throws Exception {
        givenAuthenticated();
        ReservationTimeResponse timeResponse = new ReservationTimeResponse(1L, LocalTime.of(10, 0), 1L);
        ThemeResponse themeResponse = new ThemeResponse(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        ReservationResponse reservationResponse = new ReservationResponse(
                1L, "동키", themeResponse, LocalDate.of(2026, 6, 1), timeResponse, 1L
        );
        given(reservationService.readByUserName("동키"))
                .willReturn(ReservationsResponse.from(List.of(reservationResponse)));

        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void 예약_추가() throws Exception {
        givenAuthenticated();

        Map<String, Object> body = new HashMap<>();
        body.put("themeId", 1);
        body.put("date", "2026-06-10");
        body.put("timeId", 1);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void 예약_변경() throws Exception {
        givenAuthenticated();
        ThemeResponse themeResponse = new ThemeResponse(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        ReservationTimeResponse timeResponse = new ReservationTimeResponse(2L, LocalTime.of(11, 0), 1L);
        ReservationResponse reservationResponse = new ReservationResponse(
                1L, "동키", themeResponse, LocalDate.of(2026, 6, 1), timeResponse, 1L
        );
        given(reservationService.update(anyLong(), any(), any()))
                .willReturn(reservationResponse);

        Map<String, Object> body = new HashMap<>();
        body.put("themeId", 1);
        body.put("date", "2026-06-01");
        body.put("timeId", 2);

        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void 예약_삭제() throws Exception {
        givenAuthenticated();
        willDoNothing().given(reservationService).delete(anyLong(), any());

        mockMvc.perform(delete("/api/reservations/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void 토큰_없이_예약_생성시_401() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("themeId", 1);
        body.put("date", "2026-06-10");
        body.put("timeId", 1);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 토큰_없이_예약_삭제시_401() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 토큰_없이_사용자_예약_조회시_401() throws Exception {
        mockMvc.perform(get("/api/reservations/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 지난_날짜로_예약_변경시_400() throws Exception {
        givenAuthenticated();
        willThrow(new InvalidStateException("이미 지난 날짜와 시간입니다."))
                .given(reservationService).update(anyLong(), any(), any());

        Map<String, Object> body = new HashMap<>();
        body.put("themeId", 1);
        body.put("date", "2020-01-01");
        body.put("timeId", 1);

        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 다른_사용자_예약_삭제_요청시_403() throws Exception {
        givenAuthenticated();
        willThrow(new ForbiddenException("본인의 예약만 삭제할 수 있습니다."))
                .given(reservationService).delete(anyLong(), any());

        mockMvc.perform(delete("/api/reservations/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void 존재하지_않는_예약_변경시_404() throws Exception {
        givenAuthenticated();
        willThrow(new NotFoundException("예약을 찾을 수 없습니다."))
                .given(reservationService).update(anyLong(), any(), any());

        Map<String, Object> body = new HashMap<>();
        body.put("themeId", 1);
        body.put("date", "2026-06-10");
        body.put("timeId", 1);

        mockMvc.perform(put("/api/reservations/1")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 중복_예약_추가시_409() throws Exception {
        givenAuthenticated();
        willThrow(new AlreadyInUseException("이미 예약된 테마•날짜•시간입니다."))
                .given(reservationService).create(any(), any());

        Map<String, Object> body = new HashMap<>();
        body.put("themeId", 1);
        body.put("date", "2026-06-10");
        body.put("timeId", 1);

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}
