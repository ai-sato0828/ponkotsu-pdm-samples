package info.syachiku.tomapnull;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserMapsTest {
    private static List<User> nullableUsers() {
        return List.of(new User("U001", "田中"), new User("U002", null));
    }

    @Test
    void twoArgumentsWorksForNonNullNames() {
        assertEquals(Map.of("U001", "田中", "U002", "鈴木"),
                UserMaps.toMapTwoArguments(List.of(
                        new User("U001", "田中"), new User("U002", "鈴木"))));
    }

    // BEGIN null-reproduction-tests
    @Test
    void twoArgumentsRejectsNullMappedValueWithUniqueIds() {
        assertThrows(NullPointerException.class,
                () -> UserMaps.toMapTwoArguments(nullableUsers()));
    }

    @Test
    void hashMapSupplierStillRejectsNullMappedValue() {
        assertThrows(NullPointerException.class,
                () -> UserMaps.toMapWithHashMapSupplier(nullableUsers()));
    }
    // END null-reproduction-tests

    @Test
    void filteringNullNamesRemovesTheId() {
        Map<String, String> result = UserMaps.withoutUnsetNames(nullableUsers());
        assertEquals(Map.of("U001", "田中"), result);
        assertFalse(result.containsKey("U002"));
    }

    @Test
    void defaultPolicyReplacesNullWithAString() {
        assertEquals(Map.of("U001", "田中", "U002", "未設定"),
                UserMaps.withDefaultName(nullableUsers(), "未設定"));
    }

    // BEGIN preserved-null-test
    @Test
    void preservesTheIdAndDistinguishesUnsetFromMissing() {
        Map<String, String> result = UserMaps.preservingUnsetNames(nullableUsers());
        assertEquals(2, result.size());
        assertEquals("田中", result.get("U001"));
        assertTrue(result.containsKey("U002"));
        assertNull(result.get("U002"));
        assertFalse(result.containsKey("U999"));
        assertNull(result.get("U999"));
    }
    // END preserved-null-test

    @Test
    void rejectsDuplicateIdsWithTwoNames() {
        assertThrows(IllegalArgumentException.class,
                () -> UserMaps.preservingUnsetNames(List.of(
                        new User("U001", "田中"), new User("U001", "鈴木"))));
    }

    // BEGIN duplicate-first-null-test
    @Test
    void rejectsDuplicateIdEvenWhenTheFirstNameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UserMaps.preservingUnsetNames(List.of(
                        new User("U002", null), new User("U002", "鈴木"))));
    }
    // END duplicate-first-null-test

    @Test
    void rejectsDuplicateIdsWithBothNamesNull() {
        assertThrows(IllegalArgumentException.class,
                () -> UserMaps.preservingUnsetNames(List.of(
                        new User("U002", null), new User("U002", null))));
    }

    // BEGIN put-if-absent-counterexample
    @Test
    void putIfAbsentCannotDetectAnExistingNullValue() {
        Map<String, String> namesById = new HashMap<>();
        namesById.put("U002", null);

        String previous = namesById.putIfAbsent("U002", "鈴木");

        assertNull(previous); // 未登録時と同じ戻り値なので、重複判定には使えない。
        assertEquals("鈴木", namesById.get("U002")); // 既存nullが置換される。
    }
    // END put-if-absent-counterexample

    @Test
    void buildingAndEditingTheResultDoesNotChangeTheInput() {
        List<User> users = new ArrayList<>(nullableUsers());
        List<User> before = new ArrayList<>(users);
        Map<String, String> result = UserMaps.preservingUnsetNames(users);
        result.put("U002", "更新後");
        result.remove("U001");
        assertEquals(before, users);
    }

    @Test
    void emptyInputProducesAnEmptyMap() {
        assertTrue(UserMaps.preservingUnsetNames(List.of()).isEmpty());
    }
}
