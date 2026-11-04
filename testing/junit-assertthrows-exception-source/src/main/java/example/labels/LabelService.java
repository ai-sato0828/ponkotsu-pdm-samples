package example.labels;

final class LabelService {
    private static final int MAX_GRAMS = 2_000;

    String createLabel(PostalCode destination, int grams) {
        if (grams <= 0) {
            throw new IllegalArgumentException("重量は1g以上です");
        }
        if (grams > MAX_GRAMS) {
            throw new IllegalArgumentException("重量は2000g以下です");
        }
        return destination.value() + " / " + grams + "g";
    }
}
