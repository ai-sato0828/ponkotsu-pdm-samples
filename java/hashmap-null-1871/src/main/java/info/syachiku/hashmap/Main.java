package info.syachiku.hashmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Map<String, Integer> scores = MapExamples.scoreFixture();
        for (String name : List.of("Carol", "Alice", "Bob")) {
            System.out.println(name + " get=" + scores.get(name)
                    + " containsKey=" + scores.containsKey(name)
                    + " getOrDefault=" + scores.getOrDefault(name, -1)
                    + " message=" + MapExamples.describeScore(scores, name));
        }
        CountingUserRepository nullRepository = new CountingUserRepository(Map.of());
        CountingUserRepository optionalRepository = new CountingUserRepository(Map.of());
        Map<Long, User> nullableCache = new HashMap<>();
        Map<Long, Optional<User>> optionalCache = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            UserCaches.findWithNullableCache(nullableCache, 2L, nullRepository);
            UserCaches.findWithOptionalCache(optionalCache, 2L, optionalRepository);
        }
        System.out.println("missing3 nullableCalls=" + nullRepository.callsFor(2L)
                + " optionalCalls=" + optionalRepository.callsFor(2L));
        System.out.println("identity=" + MapExamples.identityKeyObservation());
        System.out.println("mutable JDK17 observation=" + MapExamples.mutableKeyObservation());
        System.out.println("immutable=" + MapExamples.immutableKeyObservation());
    }
}
