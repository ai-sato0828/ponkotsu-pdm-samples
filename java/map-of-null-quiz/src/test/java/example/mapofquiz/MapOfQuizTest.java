package example.mapofquiz;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapOfQuizTest {
    private final AttributeMapper mapper = new AttributeMapper();

    @Test
    void 任意項目がnullならMapから除外する() {
        Map<String, String> actual = mapper.toAttributes(
                new UserForm("営業", null));

        assertEquals("営業", actual.get("department"));
        assertFalse(actual.containsKey("extension"));
    }

    @Test
    void quizThrowsBeforeLookingUpValue() {
        assertThrows(NullPointerException.class, MapOfQuiz::quiz);
    }

    @Test
    void explicitlyNullVariableAlsoThrowsWhenBuildingMap() {
        assertThrows(NullPointerException.class, MapOfQuiz::explicitNullValue);
    }

    @Test
    void hashMapDistinguishesStoredNullFromMissingKey() {
        Map<String, String> map = MapOfQuiz.hashMapAllowsNull();
        assertNull(map.get("A"));
        assertNull(map.get("B"));
        assertTrue(map.containsKey("A"));
        assertFalse(map.containsKey("B"));
    }

    @Test
    void formWithMissingExtensionCannotBuildOriginalMap() {
        var form = new UserForm("営業", null);
        assertThrows(NullPointerException.class, () -> MapOfQuiz.attributesBefore(form));
    }

    @Test
    void formWithExtensionCanBuildOriginalMap() {
        var form = new UserForm("営業", "1234");
        assertEquals(Map.of("department", "営業", "extension", "1234"),
                MapOfQuiz.attributesBefore(form));
    }

    @Test
    void mutableCopyKeepsStatusAndAddsOwner() {
        assertEquals(Map.of("status", "NEW", "owner", "U001"), MapOfQuiz.mutableCopy());
    }

    @Test
    void fixedMapRejectsPut() {
        Map<String, String> fixed = Map.of("status", "NEW");
        assertThrows(UnsupportedOperationException.class, () -> fixed.put("owner", "U001"));
    }

    @Test
    void optionalDtoCanRepresentMissingAndPresentExtensions() {
        var missing = MapOfQuiz.optionalAttribute("営業", null);
        var present = MapOfQuiz.optionalAttribute("営業", "1234");
        assertEquals("営業", missing.department());
        assertTrue(missing.extension().isEmpty());
        assertEquals("1234", present.extension().orElseThrow());
    }

    @Test
    void mapperKeepsPresentExtension() {
        var form = new UserForm("営業", "1234");
        assertEquals(Map.of("department", "営業", "extension", "1234"), mapper.toAttributes(form));
    }

    @Test
    void mapOfRejectsNullKeysAndDuplicateKeys() {
        assertThrows(NullPointerException.class, () -> Map.of(null, "NEW"));
        assertThrows(IllegalArgumentException.class, () -> Map.of("status", "NEW", "status", "DONE"));
    }
}
