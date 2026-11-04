package example.quote;

public final class QuoteCalculator {
    public Quote calculate(int merchandiseYen, Quote.Delivery delivery) {
        int feeYen = delivery == null ? 0 : delivery.feeYen();
        return new Quote(Math.addExact(merchandiseYen, feeYen), delivery);
    }
}
