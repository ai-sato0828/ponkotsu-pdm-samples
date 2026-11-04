package example.orderquote;

public record LineItem(int quantity, int unitPrice) {
    public LineItem {
        if (quantity < 1 || quantity > 100) {
            throw new InvalidQuoteException("数量は1〜100です");
        }
        if (unitPrice < 0 || unitPrice > 1_000_000) {
            throw new InvalidQuoteException("単価は0〜1,000,000円です");
        }
    }

    public long amount() {
        return (long) quantity * unitPrice;
    }
}
