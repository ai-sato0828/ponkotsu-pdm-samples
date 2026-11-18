import java.util.List;

final class OrderBatch {
    private final List<OrderLine> lines;

    OrderBatch(List<OrderLine> source) {
        this.lines = List.copyOf(source);
    }

    List<OrderLine> getLines() {
        return lines;
    }
}

final class OrderLine {
    private int quantity;

    OrderLine(int quantity) {
        this.quantity = quantity;
    }

    int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
