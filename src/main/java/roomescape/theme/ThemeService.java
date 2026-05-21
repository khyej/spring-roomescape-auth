package roomescape.theme;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.LoginUser;
import roomescape.exception.AlreadyInUseException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.dto.PageThemesResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.dto.ThemesResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private static final int POPULAR_PERIOD = 7;
    private static final int POPULAR_OFFSET = 1;
    private static final int POPULAR_LIMIT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public ThemeResponse create(LoginUser loginUser, ThemeRequest themeRequest) {
        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 테마를 생성할 수 있습니다.");
        }

        Long storeId = loginUser.isAdmin() ? themeRequest.storeId() : loginUser.storeId();
        if (storeId == null) {
            throw new IllegalArgumentException("매장 정보를 입력하세요.");
        }

        Theme theme = new Theme(
                themeRequest.name(),
                themeRequest.description(),
                themeRequest.thumbnail(),
                storeId
        );

        Theme saved = themeRepository.save(theme);
        return ThemeResponse.from(saved);
    }

    public PageThemesResponse read(Long storeId, int page, int size) {
        List<Theme> themes;
        if (storeId == null) {
            themes = themeRepository.findAll(page, size + 1);
        } else {
            themes = themeRepository.findAllByStoreId(storeId, page, size + 1);
        }

        List<ThemeResponse> themesResponse = themes.stream()
                .map(ThemeResponse::from)
                .toList();

        boolean hasNext = themesResponse.size() > size;
        if (hasNext) {
            themesResponse = themesResponse.subList(0, size);
        }

        return PageThemesResponse.from(themesResponse, page, themesResponse.size(), hasNext);
    }

    public PageThemesResponse readByStore(LoginUser loginUser, int page, int size) {
        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 테마 목록을 조회할 수 있습니다.");
        }
        List<Theme> themes;
        if (loginUser.isAdmin()) {
            themes = themeRepository.findAll(page, size + 1);
        } else {
            themes = themeRepository.findAllByStoreId(loginUser.storeId(), page, size + 1);
        }

        List<ThemeResponse> themesResponse = themes.stream()
                .map(ThemeResponse::from)
                .toList();

        boolean hasNext = themesResponse.size() > size;
        if (hasNext) {
            themesResponse = themesResponse.subList(0, size);
        }

        return PageThemesResponse.from(themesResponse, page, themesResponse.size(), hasNext);
    }

    @Transactional
    public void delete(LoginUser loginUser, Long id) {
        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 테마를 삭제할 수 있습니다.");
        }
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));

        if (!loginUser.isAdmin() && !theme.getStoreId().equals(loginUser.storeId())) {
            throw new ForbiddenException("해당 매장의 테마만 삭제할 수 있습니다.");
        }

        if (reservationRepository.existsByThemeId(id)) {
            throw new AlreadyInUseException("테마에 해당하는 예약이 있습니다.");
        }
        themeRepository.deleteById(id);
    }

    public ThemesResponse readPopularThemes() {
        LocalDate now = LocalDate.now(clock);
        LocalDate start = now.minusDays(POPULAR_PERIOD);
        LocalDate end = now.minusDays(POPULAR_OFFSET);
        List<Theme> themes = themeRepository.findPopularThemes(start, end, POPULAR_LIMIT);

        List<ThemeResponse> themesResponse = themes.stream()
                .map(ThemeResponse::from)
                .toList();

        return ThemesResponse.from(themesResponse);
    }
}
