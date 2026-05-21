package roomescape.time;

import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final Long storeId;

    public ReservationTime(LocalTime startAt, Long storeId) {
        this(null, startAt, storeId);
    }

    public ReservationTime(Long id, LocalTime startAt, Long storeId) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
        this.storeId = storeId;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void validateStore(Long storeId) {
        if (this.storeId == null || !this.storeId.equals(storeId)) {
            throw new roomescape.exception.ForbiddenException("해당 매장의 시간만 관리할 수 있습니다.");
        }
    }

    private void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("예약 시간은 비어있을 수 없습니다.");
        }
    }
}
