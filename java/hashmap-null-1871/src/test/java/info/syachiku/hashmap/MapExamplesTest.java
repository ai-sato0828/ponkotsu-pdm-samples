package info.syachiku.hashmap;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MapExamplesTest {
    @Test
    void H01_get_returnsRegisteredValue() {
        assertEquals(80, MapExamples.scoreFixture().get("Carol"));
    }

    @Test
    void H02_get_returnsRegisteredNull() {
        assertNull(MapExamples.scoreFixture().get("Alice"));
    }

    @Test
    void H03_get_returnsNullForMissingKey() {
        assertNull(MapExamples.scoreFixture().get("Bob"));
    }

    @Test
    void H04_containsKey_isTrueForValue() {
        assertTrue(MapExamples.scoreFixture().containsKey("Carol"));
    }

    @Test
    void H05_containsKey_isTrueForRegisteredNull() {
        assertTrue(MapExamples.scoreFixture().containsKey("Alice"));
    }

    @Test
    void H06_containsKey_isFalseForMissingKey() {
        assertFalse(MapExamples.scoreFixture().containsKey("Bob"));
    }

    @Test
    void H07_getOrDefault_preservesValue() {
        assertEquals(80, MapExamples.scoreFixture().getOrDefault("Carol", -1));
    }

    @Test
    void H08_getOrDefault_preservesRegisteredNull() {
        assertNull(MapExamples.scoreFixture().getOrDefault("Alice", -1));
    }

    @Test
    void H09_getOrDefault_returnsDefaultOnlyWhenMissing() {
        assertEquals(-1, MapExamples.scoreFixture().getOrDefault("Bob", -1));
    }

    @Test
    void H10_describeScore_hasGradedMessage() {
        assertEquals("点数: 80", MapExamples.describeScore(MapExamples.scoreFixture(), "Carol"));
    }

    @Test
    void H11_describeScore_hasNotGradedMessage() {
        assertEquals("採点前", MapExamples.describeScore(MapExamples.scoreFixture(), "Alice"));
    }

    @Test
    void H12_describeScore_hasMissingMessage() {
        assertEquals("受験者なし", MapExamples.describeScore(MapExamples.scoreFixture(), "Bob"));
    }

    @Test
    void H13_defaultIfNull_intentionallyCombinesNullAndMissing() {
        Map<String, String> labels = MapExamples.labelFixture();
        assertNull(labels.getOrDefault("A", "未設定"));
        assertEquals("未設定", labels.getOrDefault("B", "未設定"));
        assertEquals("未設定", MapExamples.defaultIfNull(labels, "A"));
        assertEquals("未設定", MapExamples.defaultIfNull(labels, "B"));
        assertEquals("登録値", MapExamples.defaultIfNull(labels, "C"));
    }

    @Test
    void H14_gradedOnly_skipsNullButKeepsZero() {
        assertTrue(MapExamples.gradedOnly("Alice", null).isEmpty());
        assertEquals(Map.of("Alice", 0), MapExamples.gradedOnly("Alice", 0));
    }

    @Test
    void H15_nullableCache_repeatsThreeMissingQueries() {
        CountingUserRepository repository = new CountingUserRepository(Map.of());
        Map<Long, User> cache = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            assertEquals(Optional.empty(), UserCaches.findWithNullableCache(cache, 2L, repository));
        }
        assertEquals(3, repository.callsFor(2L));
        assertFalse(cache.containsKey(2L));
    }

    @Test
    void H16_optionalCache_storesMissingAndQueriesOnce() {
        CountingUserRepository repository = new CountingUserRepository(Map.of());
        Map<Long, Optional<User>> cache = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            assertEquals(Optional.empty(), UserCaches.findWithOptionalCache(cache, 2L, repository));
        }
        assertEquals(1, repository.callsFor(2L));
        assertTrue(cache.containsKey(2L));
        assertEquals(Optional.empty(), cache.get(2L));
    }

    @Test
    void H17_bothCaches_keepSameFoundUserAndQueryOnce() {
        User alice = new User(1L, "Alice");
        CountingUserRepository nullRepository = new CountingUserRepository(Map.of(1L, alice));
        CountingUserRepository optionalRepository = new CountingUserRepository(Map.of(1L, alice));
        Map<Long, User> nullableCache = new HashMap<>();
        Map<Long, Optional<User>> optionalCache = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Optional<User> actual = UserCaches.findWithNullableCache(nullableCache, 1L, nullRepository);
            assertEquals(Optional.of(alice), actual);
            assertEquals(actual, UserCaches.findWithOptionalCache(optionalCache, 1L, optionalRepository));
        }
        assertEquals(1, nullRepository.callsFor(1L));
        assertEquals(1, optionalRepository.callsFor(1L));
    }

    @Test
    void H18_existingNullAndNullCalculation_leaveEntryAndRepeatQuery() {
        CountingUserRepository repository = new CountingUserRepository(Map.of());
        Map<Long, User> cache = new HashMap<>();
        cache.put(2L, null);
        for (int i = 0; i < 3; i++) {
            assertEquals(Optional.empty(), UserCaches.findWithNullableCache(cache, 2L, repository));
        }
        assertEquals(3, repository.callsFor(2L));
        assertTrue(cache.containsKey(2L));
        assertNull(cache.get(2L));
    }

    @Test
    void H19_gradedResult_acceptsZero() {
        ScoreResult result = ScoreResult.graded(0);
        assertEquals(ScoreStatus.GRADED, result.status());
        assertEquals(0, result.score());
    }

    @Test
    void H20_notGradedResult_hasExplicitStatus() {
        ScoreResult result = ScoreResult.notGraded();
        assertEquals(ScoreStatus.NOT_GRADED, result.status());
        assertNull(result.score());
    }

    @Test
    void H21_gradedResult_rejectsNullScore() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoreResult(ScoreStatus.GRADED, null));
    }

    @Test
    void H22_notGradedResult_rejectsNumericScore() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScoreResult(ScoreStatus.NOT_GRADED, 80));
    }

    @Test
    void H23_scoreResult_rejectsNullStatus() {
        assertThrows(NullPointerException.class, () -> new ScoreResult(null, null));
    }

    @Test
    void H24_scoreBoard_preservesThreeDisplayMessages() {
        ScoreBoard board = new ScoreBoard();
        board.register("Alice", ScoreResult.notGraded());
        board.register("Carol", ScoreResult.graded(80));
        for (String name : new String[]{"Alice", "Carol", "Bob"}) {
            assertEquals(MapExamples.describeScore(MapExamples.scoreFixture(), name), board.describe(name));
        }
    }

    @Test
    void H25_scoreBoard_rejectsNullAtRegistrationBoundary() {
        ScoreBoard board = new ScoreBoard();
        assertThrows(NullPointerException.class, () -> board.register("Alice", null));
        assertThrows(NullPointerException.class, () -> board.register(null, ScoreResult.notGraded()));
        assertThrows(NullPointerException.class, () -> board.describe(null));
    }

    @Test
    void H26_identityKey_differentInstanceDoesNotMatchSameEmployeeNo() {
        MapExamples.KeyObservation actual = MapExamples.identityKeyObservation();
        assertNull(actual.value());
        assertFalse(actual.present());
        assertEquals(1, actual.size());
        Map<Keys.IdentityKey, String> users = new HashMap<>();
        Keys.IdentityKey key = new Keys.IdentityKey("E001");
        users.put(key, "Alice");
        assertEquals("Alice", users.get(key));
    }

    @Test
    void H27_immutableKeys_haveMatchingEqualsAndHashCode() {
        Keys.EmployeeKey first = new Keys.EmployeeKey("E001");
        Keys.EmployeeKey second = new Keys.EmployeeKey("E001");
        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new Keys.EmployeeKey("E002"));
    }

    @Test
    void H28_mutableKey_observationOnVerifiedJdk17() {
        // Map仕様は等価性に影響するキー変更後の動作を保証しない。
        // これは当サンプルを実行したJDK17の観測値を固定する検査。
        MapExamples.KeyObservation actual = MapExamples.mutableKeyObservation();
        assertNull(actual.value());
        assertFalse(actual.present());
        assertEquals(1, actual.size());
    }

    @Test
    void H29_immutableKey_findsDifferentInstance() {
        assertEquals(new MapExamples.KeyObservation("Alice", true, 1),
                MapExamples.immutableKeyObservation());
    }

    @Test
    void H30_nullRejection_isCheckedSeparatelyForEachImplementation() {
        assertThrows(NullPointerException.class, MapExamples::putNullIntoConcurrentMap);
        assertThrows(NullPointerException.class, MapExamples::putNullIntoMapOf);
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(null, null);
        assertTrue(hashMap.containsKey(null));
    }
}
