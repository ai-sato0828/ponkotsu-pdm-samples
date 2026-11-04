package example.labels;

record PostalCode(String value) {
    PostalCode {
        if (value == null || !value.matches("[0-9]{7}")) {
            throw new IllegalArgumentException("郵便番号は半角数字7桁です");
        }
    }
}
