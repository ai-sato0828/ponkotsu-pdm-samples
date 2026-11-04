package example.orders;

public record Order(long id, Status status, String internalReference) {
    public enum Status { WAITING, SHIPPED, CANCELED }

    public boolean canCancel() {
        return status == Status.WAITING;
    }

    public Order canceled() {
        return new Order(id, Status.CANCELED, internalReference);
    }
}
