package info.syachiku.tomap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapExamplesTest {
    private static List<OrderLine> duplicateLines() {
        return List.of(new OrderLine("A", 2), new OrderLine("B", 1), new OrderLine("A", 3));
    }

    @Test
    void T01_同じ値でも同じキーが重複すれば例外になる() {
        assertThrows(IllegalStateException.class,
                () -> MapExamples.lengthsWithoutMerge(List.of("A", "B", "A")));
    }

    @Test
    void T02_マージなしの数量変換は重複キーで止まる() {
        assertThrows(IllegalStateException.class,
                () -> MapExamples.quantitiesWithoutMerge(duplicateLines()));
    }

    @Test
    void T03_先勝ちは入力で先の数量を残す() {
        Map<String, Integer> actual = MapExamples.firstQuantities(duplicateLines());
        assertEquals(Map.of("A", 2, "B", 1), actual);
    }

    @Test
    void T04_後勝ちは入力で後の数量を残す() {
        assertEquals(Map.of("A", 3, "B", 1), MapExamples.lastQuantities(duplicateLines()));
    }

    @Test
    void T05_同じ商品コードの数量を合計する() {
        assertEquals(Map.of("A", 5, "B", 1), MapExamples.totalQuantities(duplicateLines()));
    }

    @Test
    void T06_groupingByは同じキーの全行を残す() {
        Map<String, List<OrderLine>> actual = MapExamples.groupLines(duplicateLines());
        assertEquals(List.of(2, 3), actual.get("A").stream().map(OrderLine::quantity).toList());
        assertEquals(List.of(new OrderLine("B", 1)), actual.get("B"));
        assertEquals(3, actual.values().stream().mapToInt(List::size).sum());
    }

    @Test
    void T07_入力を逆順にすると先勝ちと後勝ちの結果も入れ替わる() {
        List<OrderLine> reversed = new ArrayList<>(duplicateLines());
        Collections.reverse(reversed);
        assertEquals(3, MapExamples.firstQuantities(reversed).get("A"));
        assertEquals(2, MapExamples.lastQuantities(reversed).get("A"));
        assertEquals(5, MapExamples.totalQuantities(reversed).get("A"));
        assertEquals(List.of(3, 2), MapExamples.groupLines(reversed).get("A").stream()
                .map(OrderLine::quantity).toList());
    }

    @Test
    void T08_空の入力はどの方針でも空Mapになる() {
        assertAll(
                () -> assertTrue(MapExamples.quantitiesWithoutMerge(List.of()).isEmpty()),
                () -> assertTrue(MapExamples.firstQuantities(List.of()).isEmpty()),
                () -> assertTrue(MapExamples.lastQuantities(List.of()).isEmpty()),
                () -> assertTrue(MapExamples.totalQuantities(List.of()).isEmpty()),
                () -> assertTrue(MapExamples.groupLines(List.of()).isEmpty()));
    }

    @Test
    void T09_数値IDが一意ならgetterのUserから名前を引ける() {
        assertEquals(Map.of(1L, "Alice", 2L, "Bob"), MapExamples.namesWithoutMerge(
                List.of(new User(2L, "Bob"), new User(1L, "Alice"))));
    }

    @Test
    void T10_数値IDの重複もマージ指定なしでは止まる() {
        assertThrows(IllegalStateException.class, () -> MapExamples.namesWithoutMerge(
                List.of(new User(1L, "old"), new User(1L, "new"))));
    }

    @Test
    void T11_数値IDの先勝ちは先の名前を残す() {
        assertEquals("old", MapExamples.firstNamesById(
                List.of(new User(1L, "old"), new User(1L, "new"))).get(1L));
    }

    @Test
    void T12_社員番号が一意ならEmployeeを保存できる() {
        Employee first = new Employee("E001", "Alice");
        Employee second = new Employee("E002", "Bob");
        assertEquals(Map.of("E001", first, "E002", second), MapExamples.toEmployeeMap(List.of(first, second)));
    }

    @Test
    void T13_社員番号の重複は業務向け例外で止める() {
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MapExamples.toEmployeeMap(List.of(new Employee("E001", "A"), new Employee("E001", "B"))));
        assertEquals("社員番号が重複しています: E001", actual.getMessage());
    }

    @Test
    void T14_顧客ごとの注文を全件残す() {
        Order first = new Order("O001", "C001", "NEW");
        Order second = new Order("O002", "C002", "DONE");
        Order third = new Order("O003", "C001", "NEW");
        Map<String, List<Order>> actual = MapExamples.ordersByCustomer(List.of(first, second, third));
        assertEquals(List.of(first, third), actual.get("C001"));
        assertEquals(List.of(second), actual.get("C002"));
    }

    @Test
    void T15_状態別の件数だけを数えられる() {
        assertEquals(Map.of("NEW", 2L, "DONE", 1L), MapExamples.countByStatus(List.of(
                new Order("O001", "C001", "NEW"), new Order("O002", "C002", "DONE"),
                new Order("O003", "C001", "NEW"))));
    }

    @Test
    void T16_LinkedHashMapでキーが初めて登場した順を保つ() {
        Map<String, String> actual = MapExamples.labelsInEncounterOrder(List.of(
                new Item("B", "B-old"), new Item("A", "A"), new Item("C", "C"), new Item("B", "B-new")));
        assertEquals(List.of("B", "A", "C"), new ArrayList<>(actual.keySet()));
        assertEquals("B-old", actual.get("B"));
    }

    @Test
    void T17_同じラベルでもキーが重複した位置を末尾へ動かさない() {
        Map<String, String> actual = MapExamples.labelsInEncounterOrder(List.of(
                new Item("B", "same"), new Item("A", "A"), new Item("B", "same")));
        assertEquals(List.of("B", "A"), new ArrayList<>(actual.keySet()));
    }

    @Test
    void T18_nullの一覧は空一覧へ読み替えない() {
        assertThrows(NullPointerException.class, () -> MapExamples.firstQuantities(null));
    }

    @Test
    void T19_一覧中のnull行を許可しない() {
        List<OrderLine> lines = Arrays.asList(new OrderLine("A", 2), null);
        assertThrows(NullPointerException.class, () -> MapExamples.totalQuantities(lines));
    }

    @Test
    void T20_同名でも数値IDが違えば別のキーになる() {
        assertEquals(Map.of(1L, "same", 2L, "same"), MapExamples.namesWithoutMerge(
                List.of(new User(1L, "same"), new User(2L, "same"))));
    }
}
