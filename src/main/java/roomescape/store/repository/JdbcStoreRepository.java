package roomescape.store.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.store.Store;

@Repository
public class JdbcStoreRepository implements StoreRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Store> storeRowMapper = (rs, rowNum) -> new Store(
            rs.getLong("id"),
            rs.getString("name")
    );

    public JdbcStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Store> findAll() {
        String sql = "SELECT id, name FROM store";
        return jdbcTemplate.query(sql, storeRowMapper);
    }
}
