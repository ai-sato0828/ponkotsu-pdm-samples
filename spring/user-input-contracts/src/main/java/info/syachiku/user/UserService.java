package info.syachiku.user;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
class UserService {
    private static final int PAGE_SIZE = 2;
    private final AtomicLong sequence = new AtomicLong(10);
    private final Map<Long, StoredUser> users = new ConcurrentHashMap<>(
            Map.of(10L, new StoredUser(10, "Tanaka", "tanaka@example.com", 5, "MEMBER")));

    Optional<UserView> find(long userId) {
        return Optional.ofNullable(users.get(userId)).map(StoredUser::view);
    }

    List<UserView> search(String keyword, int page) {
        String filter = keyword.toLowerCase(Locale.ROOT);
        return users.values().stream()
                .filter(user -> user.name().toLowerCase(Locale.ROOT).contains(filter))
                .sorted(Comparator.comparingLong(StoredUser::id))
                .skip((Math.max(1, page) - 1L) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .map(StoredUser::view)
                .toList();
    }

    UserView create(String name, String email, int notificationLimit) {
        long id = sequence.incrementAndGet();
        StoredUser user = new StoredUser(id, name, email, notificationLimit, "MEMBER");
        users.put(id, user);
        return user.view();
    }

    private record StoredUser(long id, String name, String email, int notificationLimit, String role) {
        UserView view() {
            return new UserView(id, name, email, notificationLimit);
        }
    }

    record UserView(long id, String name, String email, int notificationLimit) {}
}
