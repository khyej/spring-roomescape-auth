package roomescape.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Auth;
import roomescape.auth.LoginUser;
import roomescape.reservation.dto.PageReservationsResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationsResponse;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Auth LoginUser loginUser,
            @RequestBody @Valid ReservationRequest reservationRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(loginUser, reservationRequest));
    }

    @GetMapping
    public ResponseEntity<PageReservationsResponse> read(
            @Auth LoginUser loginUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Max(100) int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.read(loginUser, page, size));
    }

    @GetMapping("/my")
    public ResponseEntity<ReservationsResponse> readMy(@Auth LoginUser loginUser) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.readByUserName(loginUser.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable long id,
            @Auth LoginUser loginUser,
            @RequestBody @Valid ReservationRequest reservationRequest
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.update(id, reservationRequest, loginUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable long id,
            @Auth LoginUser loginUser
    ) {
        reservationService.delete(id, loginUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
