package info.syachiku.hashmap;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long id);
}
