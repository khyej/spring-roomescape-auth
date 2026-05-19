package roomescape.user.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.user.User;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("username"),
            rs.getString("password")
    );

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, name, username, password FROM \"user\" WHERE username = ?";
        return jdbcTemplate.query(sql, rowMapper, username).stream().findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, name, username, password FROM \"user\" WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }
}
