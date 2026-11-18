package example;

import java.util.ArrayList;
import java.util.List;

public final class OrderRangePreview {
    private OrderRangePreview() {}

    public static List<String> preview(List<String> orders, int from, int to) {
        int size = orders.size();
        if (from < 0 || from > to || to > size) {
            throw new IllegalArgumentException(
                    "expected 0 <= from <= to <= size (from=%d, to=%d, size=%d)"
                            .formatted(from, to, size));
        }
        return orders.subList(from, to);
    }

    public static void main(String[] args) {
        var orders = new ArrayList<>(List.of("A01", "B02"));
        int[][] ranges = {{0, 2}, {2, 2}, {-1, 1}, {0, 3}, {2, 1}};

        for (int[] range : ranges) {
            int from = range[0];
            int to = range[1];
            try {
                var result = orders.subList(from, to);
                System.out.printf("subList(%d, %d) -> %s%n", from, to, result);
            } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                System.out.printf("subList(%d, %d) -> %s%n", from, to,
                        e.getClass().getSimpleName());
            }
        }

        for (int[] range : ranges) {
            int from = range[0];
            int to = range[1];
            try {
                var result = preview(orders, from, to);
                System.out.printf("preview(%d, %d) -> %s%n", from, to, result);
            } catch (IllegalArgumentException e) {
                System.out.printf("preview(%d, %d) -> %s: %s%n", from, to,
                        e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
