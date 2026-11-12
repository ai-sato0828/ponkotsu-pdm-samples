package info.syachiku.hashmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CountingUserRepository implements UserRepository {
    private final Map<Long, User> users;
    private final Map<Long, Integer> calls = new HashMap<>();

    public CountingUserRepository(Map<Long, User> users) {
        this.users = Map.copyOf(users);
    }

    @Override
    public Optional<User> findById(long id) {
        calls.merge(id, 1, Integer::sum);
        return Optional.ofNullable(users.get(id));
    }

    public int callsFor(long id) {
        return calls.getOrDefault(id, 0);
    }
}
