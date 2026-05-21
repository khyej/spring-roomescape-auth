package roomescape.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.LoginUser;
import roomescape.exception.AlreadyInUseException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.reservation.dto.PageReservationsResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationsResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse create(LoginUser loginUser, ReservationRequest reservationRequest) {
        ReservationTime reservationTime = getReservationTime(reservationRequest);
        Theme theme = getTheme(reservationRequest);

        validateStoreConsistency(theme, reservationTime);

        Reservation reservation = new Reservation(
                loginUser.name(),
                theme,
                reservationRequest.date(),
                reservationTime,
                theme.getStoreId()
        );

        reservation.validateNotPast(LocalDateTime.now(clock));
        validateDuplicate(reservation);

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    @Transactional
    public ReservationResponse update(long id, ReservationRequest reservationRequest, LoginUser loginUser) {
        Reservation reservation = getReservation(id);

        validateAccess(reservation, loginUser);
        reservation.validateNotPast(LocalDateTime.now(clock));

        ReservationTime reservationTime = getReservationTime(reservationRequest);
        Theme theme = getTheme(reservationRequest);

        validateStoreConsistency(theme, reservationTime);
        if (loginUser.isManager()) {
            theme.validateStore(loginUser.storeId());
        }

        Reservation updateReservation = new Reservation(
                id,
                reservation.getUserName(),
                theme,
                reservationRequest.date(),
                reservationTime,
                theme.getStoreId()
        );

        updateReservation.validateNotPast(LocalDateTime.now(clock));
        validateDuplicate(updateReservation);

        reservationRepository.update(updateReservation);
        return ReservationResponse.from(updateReservation);
    }

    @Transactional
    public void delete(Long id, LoginUser loginUser) {
        Reservation reservation = getReservation(id);
        validateAccess(reservation, loginUser);
        reservation.validateNotPast(LocalDateTime.now(clock));
        reservationRepository.deleteById(id);
    }

    public PageReservationsResponse read(LoginUser loginUser, int page, int size) {
        List<Reservation> reservations;
        if (loginUser.isAdmin()) {
            reservations = reservationRepository.findAll(page, size + 1);
        } else if (loginUser.isManager()) {
            reservations = reservationRepository.findAllByStoreId(loginUser.storeId(), page, size + 1);
        } else {
            throw new ForbiddenException("예약 목록 조회 권한이 없습니다.");
        }

        List<ReservationResponse> reservationsResponse = reservations.stream()
                .map(ReservationResponse::from)
                .toList();

        boolean hasNext = reservationsResponse.size() > size;
        if (hasNext) {
            reservationsResponse = reservationsResponse.subList(0, size);
        }

        return PageReservationsResponse.from(reservationsResponse, page, reservationsResponse.size(), hasNext);
    }

    public ReservationsResponse readByUserName(String userName) {
        List<ReservationResponse> reservationsResponse = reservationRepository.findByUserName(userName).stream()
                .map(ReservationResponse::from)
                .toList();

        return ReservationsResponse.from(reservationsResponse);
    }

    @Transactional
    public void deleteByAdmin(LoginUser loginUser, Long id) {
        Reservation reservation = getReservation(id);

        if (!loginUser.isManagerOrAdmin()) {
            throw new ForbiddenException("관리자만 삭제할 수 있습니다.");
        }
        if (!loginUser.isAdmin()) {
            reservation.validateStore(loginUser.storeId());
        }
        reservation.validateNotPast(LocalDateTime.now(clock));

        reservationRepository.deleteById(id);
    }

    private Reservation getReservation(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }

    private Theme getTheme(ReservationRequest reservationRequest) {
        return themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));
    }

    private ReservationTime getReservationTime(ReservationRequest reservationRequest) {
        return reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new NotFoundException("예약 시간을 찾을 수 없습니다."));
    }

    private void validateStoreConsistency(Theme theme, ReservationTime reservationTime) {
        if (!theme.getStoreId().equals(reservationTime.getStoreId())) {
            throw new roomescape.exception.InvalidStateException("테마와 예약 시간의 매장이 일치하지 않습니다.");
        }
    }

    private void validateAccess(Reservation reservation, LoginUser loginUser) {
        if (loginUser.isAdmin()) {
            return;
        }
        if (loginUser.isManager() && reservation.getStoreId().equals(loginUser.storeId())) {
            return;
        }
        reservation.validateOwner(loginUser.name());
    }

    private void validateDuplicate(Reservation reservation) {
        if (reservationRepository.existsByThemeIdAndDateAndTimeId(
                reservation.getTheme().getId(),
                reservation.getDate(),
                reservation.getTime().getId())
        ) {
            throw new AlreadyInUseException("이미 예약된 테마•날짜•시간입니다.");
        }
    }
}
