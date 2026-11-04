package example.inventory;

final class InventoryTotal {
    static long total(int boxes, int itemsPerBox) {
        requireNonNegative(boxes, itemsPerBox);
        return (long) boxes * itemsPerBox;
    }

    static int totalForIntColumn(int boxes, int itemsPerBox) {
        requireNonNegative(boxes, itemsPerBox);
        return Math.multiplyExact(boxes, itemsPerBox);
    }

    static long totalFromLongCounts(long boxes, long itemsPerBox) {
        requireNonNegative(boxes, itemsPerBox);
        return Math.multiplyExact(boxes, itemsPerBox);
    }

    private static void requireNonNegative(long boxes, long itemsPerBox) {
        if (boxes < 0 || itemsPerBox < 0) {
            throw new IllegalArgumentException("箱数と1箱あたりの個数は0以上です");
        }
    }
}
