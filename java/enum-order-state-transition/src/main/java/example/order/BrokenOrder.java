package example.order;

final class BrokenOrder {
    private OrderStatus status = OrderStatus.UNSHIPPED;

    OrderStatus status() {
        return status;
    }

    void setStatus(OrderStatus status) {
        this.status = status;
    }
}
