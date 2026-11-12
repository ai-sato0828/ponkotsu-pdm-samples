package info.syachiku.tomap;

import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        List<OrderLine> lines = List.of(
                new OrderLine("A", 2),
                new OrderLine("B", 1),
                new OrderLine("A", 3));
        try {
            MapExamples.quantitiesWithoutMerge(lines);
        } catch (IllegalStateException exception) {
            System.out.println("withoutMerge=" + exception.getClass().getSimpleName());
        }
        System.out.println("first A=" + MapExamples.firstQuantities(lines).get("A"));
        System.out.println("last A=" + MapExamples.lastQuantities(lines).get("A"));
        System.out.println("sum A=" + MapExamples.totalQuantities(lines).get("A"));
        System.out.println("group A=" + MapExamples.groupLines(lines).get("A").stream()
                .map(OrderLine::quantity).toList());
        List<Item> items = List.of(
                new Item("B", "B-old"), new Item("A", "A"),
                new Item("C", "C"), new Item("B", "B-new"));
        System.out.println("label keys=" + MapExamples.labelsInEncounterOrder(items).keySet());
    }
}
