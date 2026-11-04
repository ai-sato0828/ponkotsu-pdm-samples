package example.inventory;

final class Inventory {
    private final int destinationCapacity;
    private StockLevels levels;

    Inventory(int source, int destination, int destinationCapacity) {
        if (destinationCapacity < destination) {
            throw new IllegalArgumentException("移動先の初期在庫が上限を超えています");
        }
        this.destinationCapacity = destinationCapacity;
        this.levels = new StockLevels(source, destination);
    }

    StockLevels snapshot() {
        return levels;
    }

    void move(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("移動数は1個以上です");
        }
        StockLevels before = levels;
        if (units > before.source()) {
            throw new IllegalStateException("移動元の在庫が足りません");
        }
        if (units > destinationCapacity - before.destination()) {
            throw new IllegalStateException("移動先に空きがありません");
        }

        StockLevels after = new StockLevels(before.source() - units, before.destination() + units);
        levels = after;
    }
}
