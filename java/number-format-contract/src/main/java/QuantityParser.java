public final class QuantityParser {
    private QuantityParser() {}

    public static int parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("数量を入力してください");
        }
        // ASCII数字のみ。カンマを使うなら先頭1〜3桁、以降は3桁ずつ。
        if (!input.matches("(?:[0-9]+|[0-9]{1,3}(?:,[0-9]{3})+)")) {
            throw new IllegalArgumentException("数量は半角数字と3桁区切りのカンマで入力してください");
        }
        try {
            return Integer.parseInt(input.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数量は0〜2,147,483,647で入力してください", e);
        }
    }
}
