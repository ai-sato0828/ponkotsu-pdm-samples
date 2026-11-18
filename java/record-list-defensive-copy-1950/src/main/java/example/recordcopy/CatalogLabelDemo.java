package example.recordcopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CatalogLabelDemo {
    // recordのフィールドはfinalでも、このListの内容は共有される。
    public record SharedLabel(String sku, List<String> tags) {}

    public record SnapshotLabel(String sku, List<String> tags) {
        public SnapshotLabel {
            tags = List.copyOf(tags);
        }
    }

    public static void main(String[] args) {
        List<String> input = new ArrayList<>(List.of("java"));
        SharedLabel shared = new SharedLabel("SKU-001", input);
        SnapshotLabel snapshot = new SnapshotLabel("SKU-001", input);
        input.add("sale");
        System.out.printf("SHARED sku=%s tags=%s%n", shared.sku(), shared.tags());
        System.out.printf("SNAPSHOT sku=%s tags=%s%n", snapshot.sku(), snapshot.tags());

        try {
            snapshot.tags().add("preview");
            throw new AssertionError("Expected an unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            System.out.printf("ACCESSOR add=%s tags=%s%n",
                    expected.getClass().getSimpleName(), snapshot.tags());
        }

        List<String> withNull = Arrays.asList("java", null);
        SharedLabel nullable = new SharedLabel("SKU-001", withNull);
        try {
            new SnapshotLabel("SKU-001", withNull);
            throw new AssertionError("Expected null element rejection");
        } catch (NullPointerException expected) {
            System.out.printf("NULL_ELEMENT sharedTags=%s snapshot=%s%n",
                    nullable.tags(), expected.getClass().getSimpleName());
        }
    }
}
