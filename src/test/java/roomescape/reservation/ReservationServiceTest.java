package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.LoginUser;
import roomescape.exception.AlreadyInUseException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidStateException;
import roomescape.reservation.dto.PageReservationsResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.user.Role;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationService reservationService;

    private final Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
    private final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0), 1L);
    private final LoginUser loginUser = new LoginUser(1L, "동키", Role.USER, null);
    private final LoginUser jamsilManager = new LoginUser(2L, "잠실매니저", Role.MANAGER, 1L);
    private final LoginUser gangnamManager = new LoginUser(3L, "강남매니저", Role.MANAGER, 2L);
    private final LoginUser adminUser = new LoginUser(4L, "관리자", Role.ADMIN, null);

    @Test
    void 이미_지난_날짜로_예약_생성시_400() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        ReservationRequest request = new ReservationRequest(1L, LocalDate.of(2026, 5, 13), 1L);

        assertThatThrownBy(() -> reservationService.create(loginUser, request))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void 매장_매니저는_자기_매장_예약만_조회할_수_있다() {
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findAllByStoreId(1L, 0, 11)).willReturn(List.of(reservation));

        PageReservationsResponse response = reservationService.read(jamsilManager, 0, 10);

        assertThat(response.reservations()).hasSize(1);
        assertThat(response.reservations().get(0).id()).isEqualTo(1L);
    }

    @Test
    void 관리자는_전체_매장_예약을_조회할_수_있다() {
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findAll(0, 11)).willReturn(List.of(reservation));

        PageReservationsResponse response = reservationService.read(adminUser, 0, 10);

        assertThat(response.reservations()).hasSize(1);
    }

    @Test
    void 일반_사용자는_전체_예약_목록을_조회할_수_없다() {
        assertThatThrownBy(() -> reservationService.read(loginUser, 0, 10))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 매장_매니저는_타_매장_예약_삭제시_403() {
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteByAdmin(gangnamManager, 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 매장_매니저는_자기_매장_예약_삭제_성공() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        reservationService.deleteByAdmin(jamsilManager, 1L);
    }

    @Test
    void 관리자는_모든_매장_예약_삭제_성공() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        reservationService.deleteByAdmin(adminUser, 1L);
    }

    @Test
    void 다른_사용자의_예약_삭제시_403() {
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        LoginUser otherUser = new LoginUser(2L, "그해", Role.USER, null);

        assertThatThrownBy(() -> reservationService.delete(1L, otherUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 테마와_시간의_매장이_다르면_예약_생성_실패() {
        Theme otherStoreTheme = new Theme(2L, "다른 매장 테마", "설명", "http://s3.com", 2L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime)); // storeId = 1
        given(themeRepository.findById(2L)).willReturn(Optional.of(otherStoreTheme)); // storeId = 2

        ReservationRequest request = new ReservationRequest(2L, LocalDate.of(2026, 5, 20), 1L);

        assertThatThrownBy(() -> reservationService.create(loginUser, request))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void 예약_변경_성공() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        ReservationRequest request = new ReservationRequest(1L, LocalDate.of(2026, 5, 21), 1L);

        reservationService.update(1L, request, loginUser);
    }

    @Test
    void 매장_매니저가_다른_매장_테마로_변경_시도시_403() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));
        Reservation reservation = new Reservation(1L, "동키", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L);
        Theme otherStoreTheme = new Theme(2L, "다른 매장 테마", "설명", "http://s3.com", 2L);
        ReservationTime otherStoreTime = new ReservationTime(2L, LocalTime.of(11, 0), 2L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(themeRepository.findById(2L)).willReturn(Optional.of(otherStoreTheme));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(otherStoreTime));

        ReservationRequest request = new ReservationRequest(2L, LocalDate.of(2026, 5, 21), 2L);

        assertThatThrownBy(() -> reservationService.update(1L, request, jamsilManager))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 매장_매니저가_본인의_타_매장_예약_삭제_성공() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));
        LoginUser gangnamManagerMe = new LoginUser(3L, "강남매니저", Role.MANAGER, 2L);
        Reservation reservation = new Reservation(1L, "강남매니저", theme, LocalDate.of(2026, 5, 20), reservationTime, 1L); // storeId = 1
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        reservationService.delete(1L, gangnamManagerMe);
    }

    @Test
    void 중복_예약_생성시_409() {
        mockTime(LocalDate.of(2026, 5, 14), LocalTime.of(12, 0));

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.existsByThemeIdAndDateAndTimeId(anyLong(), any(LocalDate.class), anyLong()))
                .willReturn(true);

        ReservationRequest request = new ReservationRequest(1L, LocalDate.of(2026, 5, 20), 1L);

        assertThatThrownBy(() -> reservationService.create(loginUser, request))
                .isInstanceOf(AlreadyInUseException.class);
    }

    private void mockTime(LocalDate date, LocalTime time) {
        Instant fixedInstant = date.atTime(time).atZone(ZoneId.of("Asia/Seoul")).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
    }
}
