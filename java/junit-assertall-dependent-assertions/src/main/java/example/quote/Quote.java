package example.quote;

public record Quote(int totalYen, Delivery delivery) {
    public record Delivery(Method method, int feeYen) {}

    public enum Method {
        STANDARD, EXPRESS
    }
}
