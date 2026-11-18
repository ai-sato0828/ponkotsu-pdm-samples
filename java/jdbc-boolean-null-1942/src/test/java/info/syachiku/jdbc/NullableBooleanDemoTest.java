package info.syachiku.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullableBooleanDemoTest {
    @Test
    void primitiveGetterCollapsesSqlNullAndFalse() throws SQLException {
        var readings = NullableBooleanDemo.observe();

        assertEquals(List.of(1, 2, 3), readings.stream().map(NullableBooleanDemo.Reading::id).toList());
        assertEquals(List.of(true, false, false), readings.stream().map(NullableBooleanDemo.Reading::primitive).toList());
        assertEquals(List.of(false, false, true), readings.stream().map(NullableBooleanDemo.Reading::wasNullForActive).toList());
    }

    @Test
    void wasNullUsesTheMostRecentColumnGetter() throws SQLException {
        var unknown = NullableBooleanDemo.observe().get(2);

        assertEquals(3, unknown.id());
        assertTrue(unknown.wasNullForActive());
        assertFalse(unknown.wasNullAfterId());
    }

    @Test
    void typedGetterAndImmediateWasNullPreserveAllThreeStates() throws SQLException {
        var readings = NullableBooleanDemo.observe();
        List<Boolean> expected = Arrays.asList(true, false, null);

        assertEquals(expected, readings.stream().map(NullableBooleanDemo.Reading::typed).toList());
        assertEquals(expected, readings.stream().map(NullableBooleanDemo.Reading::restored).toList());
    }

    @Test
    void trueEqualsOnlySelectsEnabledRows() throws SQLException {
        var readings = NullableBooleanDemo.observe();
        Boolean disabled = readings.get(1).typed();
        Boolean unknown = readings.get(2).typed();

        assertTrue(Boolean.TRUE.equals(readings.get(0).typed()));
        assertFalse(Boolean.TRUE.equals(disabled));
        assertFalse(Boolean.TRUE.equals(unknown));
        assertTrue(Boolean.FALSE.equals(disabled));
        assertNull(unknown);
    }
}
