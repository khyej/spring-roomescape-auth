package roomescape.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.LoginUser;
import roomescape.reservation.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.dto.ReservationTimeRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.dto.ReservationTimesResponse;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.user.Role;

@ExtendWith(MockitoExtension.class)
class ReservationTimeServiceTest {

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private java.time.Clock clock;

    @InjectMocks
    private ReservationTimeService reservationTimeService;

    @Test
    void 예약_시간_생성() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0), 1L);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0), 1L);
        LoginUser loginUser = new LoginUser(1L, "잠실매니저", Role.MANAGER, 1L);

        given(reservationTimeRepository.save(any(ReservationTime.class))).willReturn(reservationTime);

        ReservationTimeResponse response = reservationTimeService.create(loginUser, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 예약_시간_삭제() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0), 1L);
        LoginUser loginUser = new LoginUser(1L, "잠실매니저", Role.MANAGER, 1L);

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(reservationRepository.existsByTimeId(1L)).willReturn(false);

        reservationTimeService.delete(loginUser, 1L);

        verify(reservationTimeRepository).deleteById(1L);
    }

    @Test
    void 예약_가능_시간_조회() {
        Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0), 1L);
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(11, 0), 1L);
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), time1, 1L);

        java.time.Instant instant = java.time.LocalDateTime.of(2026, 5, 20, 9, 0).atZone(java.time.ZoneId.of("Asia/Seoul")).toInstant();
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(java.time.ZoneId.of("Asia/Seoul"));

        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.findByThemeAndDate(1L, LocalDate.of(2026, 5, 20)))
                .willReturn(List.of(reservation));
        given(reservationTimeRepository.findAllByStoreId(1L)).willReturn(List.of(time1, time2));

        ReservationTimesResponse response = reservationTimeService.readAvailableTimes(1L, LocalDate.of(2026, 5, 20));

        assertThat(response.reservationTimes()).hasSize(1);
        assertThat(response.reservationTimes().get(0).startAt()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    void 오늘_날짜의_지난_시간은_조회되지_않는다() {
        Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.of(10, 0), 1L);
        ReservationTime futureTime = new ReservationTime(2L, LocalTime.of(11, 0), 1L);

        java.time.Instant instant = java.time.LocalDateTime.of(2026, 5, 20, 10, 30).atZone(java.time.ZoneId.of("Asia/Seoul")).toInstant();
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(java.time.ZoneId.of("Asia/Seoul"));

        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.findByThemeAndDate(1L, LocalDate.of(2026, 5, 20)))
                .willReturn(List.of());
        given(reservationTimeRepository.findAllByStoreId(1L)).willReturn(List.of(pastTime, futureTime));

        ReservationTimesResponse response = reservationTimeService.readAvailableTimes(1L, LocalDate.of(2026, 5, 20));

        assertThat(response.reservationTimes()).hasSize(1);
        assertThat(response.reservationTimes().get(0).startAt()).isEqualTo(LocalTime.of(11, 0));
    }
}
