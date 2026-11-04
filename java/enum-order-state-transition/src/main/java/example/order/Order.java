package example.order;

final class Order {
    private OrderStatus status = OrderStatus.UNSHIPPED;

    OrderStatus status() {
        return status;
    }

    void ship() {
        transitionFromUnshipped(OrderStatus.SHIPPED);
    }

    void cancel() {
        transitionFromUnshipped(OrderStatus.CANCELLED);
    }

    private void transitionFromUnshipped(OrderStatus next) {
        if (status != OrderStatus.UNSHIPPED) {
            throw new IllegalStateException("未出荷の注文だけを変更できます");
        }
        status = next;
    }
}
