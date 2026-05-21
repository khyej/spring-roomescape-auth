package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.LoginUser;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.dto.ThemesResponse;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.Role;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 테마_생성() {
        ThemeRequest request = new ThemeRequest("공포의 방", "무서운 방", "http://s3.com", 1L);
        Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        LoginUser loginUser = new LoginUser(1L, "잠실매니저", Role.MANAGER, 1L);

        given(themeRepository.save(any(Theme.class))).willReturn(theme);

        ThemeResponse response = themeService.create(loginUser, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("공포의 방");
    }

    @Test
    void 테마_삭제() {
        Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        LoginUser loginUser = new LoginUser(1L, "잠실매니저", Role.MANAGER, 1L);

        given(themeRepository.findById(1L)).willReturn(java.util.Optional.of(theme));
        given(reservationRepository.existsByThemeId(1L)).willReturn(false);

        themeService.delete(loginUser, 1L);

        verify(themeRepository).deleteById(1L);
    }

    @Test
    void 인기_테마_조회() {
        LocalDate now = LocalDate.of(2026, 5, 20);
        Instant instant = now.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));

        Theme theme = new Theme(1L, "공포의 방", "무서운 방", "http://s3.com", 1L);
        given(themeRepository.findPopularThemes(any(), any(), any(Integer.class)))
                .willReturn(List.of(theme));

        ThemesResponse response = themeService.readPopularThemes();

        assertThat(response.themes()).hasSize(1);
        assertThat(response.themes().get(0).name()).isEqualTo("공포의 방");
    }
}
