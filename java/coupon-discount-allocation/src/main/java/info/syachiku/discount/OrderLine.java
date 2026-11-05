package info.syachiku.discount;

public record OrderLine(String id, int subtotalYen) {
    public OrderLine {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("明細IDは必須です");
        }
        if (subtotalYen < 0) {
            throw new IllegalArgumentException("明細の小計は0円以上です");
        }
    }
}
