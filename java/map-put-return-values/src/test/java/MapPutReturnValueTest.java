import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapPutReturnValueTest {
    @Test
    void firstPutReturnsNullAndStoresOne() {
        Map<String, Integer> map = new HashMap<>();
        assertFalse(map.containsKey("A"));
        assertNull(map.get("A"));
        assertEquals(0, map.size());

        Integer previous = map.put("A", 1);

        assertNull(previous);
        assertTrue(map.containsKey("A"));
        assertEquals(Integer.valueOf(1), map.get("A"));
        assertEquals(1, map.size());
    }

    @Test
    void overwriteReturnsFiveAndStoresOne() {
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 5);
        assertTrue(map.containsKey("A"));
        assertEquals(Integer.valueOf(5), map.get("A"));
        assertEquals(1, map.size());

        Integer previous = map.put("A", 1);

        assertEquals(Integer.valueOf(5), previous);
        assertTrue(map.containsKey("A"));
        assertEquals(Integer.valueOf(1), map.get("A"));
        assertEquals(1, map.size());
    }

    @Test
    void replacingStoredNullReturnsNullAndStoresOne() {
        Map<String, Integer> map = new HashMap<>();
        map.put("A", null); // 操作前からAが存在し、値がnull。
        assertTrue(map.containsKey("A"));
        assertNull(map.get("A"));
        assertEquals(1, map.size());

        Integer previous = map.put("A", 1);

        assertNull(previous);
        assertTrue(map.containsKey("A"));
        assertEquals(Integer.valueOf(1), map.get("A"));
        assertEquals(1, map.size());
    }

    @Test
    void puttingNullAddsKeyEvenThoughGetStillReturnsNull() {
        Map<String, Integer> map = new HashMap<>();
        assertFalse(map.containsKey("A"));
        assertNull(map.get("A"));
        assertEquals(0, map.size());

        Integer previous = map.put("A", null); // 今回の操作でnullを登録する。

        assertNull(previous);
        assertTrue(map.containsKey("A"));
        assertNull(map.get("A"));
        assertEquals(1, map.size());
    }
}
