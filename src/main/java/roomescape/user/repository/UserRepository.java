package roomescape.user.repository;

import java.util.Optional;
import roomescape.user.User;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);
}
