package roomescape.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Auth;
import roomescape.auth.LoginUser;
import roomescape.time.ReservationTimeService;
import roomescape.time.dto.ReservationTimeRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.dto.ReservationTimesResponse;

@RestController
@RequestMapping("/api/admin/times")
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<ReservationTimesResponse> read(@Auth LoginUser loginUser) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationTimeService.readByStore(loginUser));
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @Auth LoginUser loginUser,
            @Valid @RequestBody ReservationTimeRequest reservationTimeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationTimeService.create(loginUser, reservationTimeRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Auth LoginUser loginUser, @PathVariable Long id) {
        reservationTimeService.delete(loginUser, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
