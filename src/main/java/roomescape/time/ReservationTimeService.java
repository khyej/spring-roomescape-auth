package roomescape.time;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.LoginUser;
import roomescape.exception.AlreadyInUseException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.dto.ReservationTimeRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.dto.ReservationTimesResponse;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final java.time.Clock clock;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository,
                                  ThemeRepository themeRepository,
                                  java.time.Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationTimeResponse create(LoginUser loginUser, ReservationTimeRequest reservationTimeRequest) {
        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 예약 시간을 생성할 수 있습니다.");
        }

        Long storeId = loginUser.isAdmin() ? reservationTimeRequest.storeId() : loginUser.storeId();
        if (storeId == null) {
            throw new IllegalArgumentException("매장 정보를 입력하세요.");
        }

        ReservationTime reservationTime = new ReservationTime(
                reservationTimeRequest.startAt(),
                storeId
        );

        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(saved);
    }

    public ReservationTimesResponse read() {
        List<ReservationTimeResponse> reservationTimesResponse = reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();

        return ReservationTimesResponse.from(reservationTimesResponse);
    }

    public ReservationTimesResponse readByStore(LoginUser loginUser) {
        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 예약 시간을 조회할 수 있습니다.");
        }
        List<ReservationTime> times;
        if (loginUser.isAdmin()) {
            times = reservationTimeRepository.findAll();
        } else {
            times = reservationTimeRepository.findAllByStoreId(loginUser.storeId());
        }

        List<ReservationTimeResponse> reservationTimesResponse = times.stream()
                .map(ReservationTimeResponse::from)
                .toList();

        return ReservationTimesResponse.from(reservationTimesResponse);
    }

    @Transactional
    public void delete(LoginUser loginUser, Long id) {
        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 예약 시간을 삭제할 수 있습니다.");
        }
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약 시간을 찾을 수 없습니다."));

        if (!loginUser.isAdmin() && !reservationTime.getStoreId().equals(loginUser.storeId())) {
            throw new ForbiddenException("해당 매장의 예약 시간만 삭제할 수 있습니다.");
        }

        if (reservationRepository.existsByTimeId(id)) {
            throw new AlreadyInUseException("예약 시간에 해당하는 예약이 있습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }

    public ReservationTimesResponse readAvailableTimes(Long themeId, LocalDate date) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today)) {
            return ReservationTimesResponse.from(List.of());
        }

        List<LocalTime> reservedTimes = reservationRepository.findByThemeAndDate(themeId, date).stream()
                .map(m -> m.getTime().getStartAt())
                .toList();

        LocalTime now = LocalTime.now(clock);
        List<ReservationTime> availableTimes = reservationTimeRepository.findAllByStoreId(theme.getStoreId()).stream()
                .filter(r -> !reservedTimes.contains(r.getStartAt()))
                .filter(r -> !date.equals(today) || r.getStartAt().isAfter(now))
                .toList();

        List<ReservationTimeResponse> reservationTimesResponse = availableTimes.stream()
                .map(ReservationTimeResponse::from).toList();

        return ReservationTimesResponse.from(reservationTimesResponse);
    }
}
