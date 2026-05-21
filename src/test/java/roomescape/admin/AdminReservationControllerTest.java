package roomescape.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthService;
import roomescape.config.AuthArgumentResolver;
import roomescape.exception.InvalidStateException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.ReservationService;
import roomescape.user.User;

@WebMvcTest(AdminReservationController.class)
@Import({AuthArgumentResolver.class})
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AuthService authService;

    private static final String TOKEN = "Bearer test-token";
    private static final User TEST_USER = new User(1L, "관리자", "admin", "admin1234", roomescape.user.Role.MANAGER, 1L);

    private void givenAuthenticated() {
        given(authService.authenticate("test-token")).willReturn(TEST_USER);
    }

    @Test
    void 예약_삭제() throws Exception {
        givenAuthenticated();

        mockMvc.perform(delete("/api/admin/reservations/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void 지난_날짜_예약_삭제시_400() throws Exception {
        givenAuthenticated();
        willThrow(new InvalidStateException("이미 지난 날짜와 시간입니다."))
                .given(reservationService).deleteByAdmin(any(), anyLong());

        mockMvc.perform(delete("/api/admin/reservations/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_예약_삭제시_404() throws Exception {
        givenAuthenticated();
        willThrow(new NotFoundException("예약을 찾을 수 없습니다."))
                .given(reservationService).deleteByAdmin(any(), anyLong());

        mockMvc.perform(delete("/api/admin/reservations/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void 토큰_없이_삭제_요청시_401() throws Exception {
        mockMvc.perform(delete("/api/admin/reservations/1"))
                .andExpect(status().isUnauthorized());
    }
}
