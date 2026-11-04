package example.inventory;

record StockLevels(int source, int destination) {
    StockLevels {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException("在庫数は0個以上です");
        }
    }
}
