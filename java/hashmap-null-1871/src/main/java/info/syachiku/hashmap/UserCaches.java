package info.syachiku.hashmap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class UserCaches {
    private UserCaches() {
    }

    public static Optional<User> findWithNullableCache(
            Map<Long, User> cache, long userId, UserRepository repository) {
        Objects.requireNonNull(cache, "cache");
        Objects.requireNonNull(repository, "repository");
        User user = cache.computeIfAbsent(
                userId, id -> repository.findById(id).orElse(null));
        return Optional.ofNullable(user);
    }

    public static Optional<User> findWithOptionalCache(
            Map<Long, Optional<User>> cache, long userId, UserRepository repository) {
        Objects.requireNonNull(cache, "cache");
        Objects.requireNonNull(repository, "repository");
        return cache.computeIfAbsent(userId, repository::findById);
    }
}
