package example.inventory;

final class BrokenInventoryTotal {
    static long total(int boxes, int itemsPerBox) {
        long result = boxes * itemsPerBox;
        return result;
    }
}
