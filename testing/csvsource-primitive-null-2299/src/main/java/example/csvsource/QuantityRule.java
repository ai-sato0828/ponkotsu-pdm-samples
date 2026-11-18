package example.csvsource;

final class QuantityRule {
    private QuantityRule() {
    }

    static int normalize(Integer quantity) {
        return quantity == null ? 0 : quantity;
    }
}
