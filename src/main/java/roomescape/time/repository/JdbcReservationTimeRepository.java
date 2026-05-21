package roomescape.time.repository;

import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.time.ReservationTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getObject("start_at", LocalTime.class),
            resultSet.getLong("store_id")
    );

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time(start_at, store_id) VALUES(?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement psmt = con.prepareStatement(sql, new String[]{"id"});
            psmt.setObject(1, reservationTime.getStartAt());
            psmt.setLong(2, reservationTime.getStoreId());
            return psmt;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new ReservationTime(id, reservationTime.getStartAt(), reservationTime.getStoreId());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at, store_id FROM reservation_time";
        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public List<ReservationTime> findAllByStoreId(Long storeId) {
        String sql = "SELECT id, start_at, store_id FROM reservation_time WHERE store_id = ?";
        return jdbcTemplate.query(sql, reservationTimeRowMapper, storeId);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        String sql = "SELECT id, start_at, store_id FROM reservation_time WHERE id = ?";
        List<ReservationTime> reservationTimes = jdbcTemplate.query(sql, reservationTimeRowMapper, id);
        return reservationTimes.stream().findFirst();
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
