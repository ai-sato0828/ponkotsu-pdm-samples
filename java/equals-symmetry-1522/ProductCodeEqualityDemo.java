import java.util.Objects;

public class ProductCodeEqualityDemo {
    static class OpenProductCode {
        private final String value;

        OpenProductCode(String value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof OpenProductCode other)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    static final class RegionalProductCode extends OpenProductCode {
        private final String region;

        RegionalProductCode(String value, String region) {
            super(value);
            this.region = Objects.requireNonNull(region);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RegionalProductCode other
                    && super.equals(other)
                    && region.equals(other.region);
        }
    }

    public static final class ProductCode {
        private final String value;

        public ProductCode(String value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ProductCode other)) {
                return false;
            }
            return Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static void main(String[] args) {
        var open = new OpenProductCode("P001");
        var regional = new RegionalProductCode("P001", "JP");
        check("修正前の親→子", open.equals(regional), true);
        check("修正前の子→親", regional.equals(open), false);

        var first = new ProductCode("P001");
        var second = new ProductCode("P001");
        var different = new ProductCode("P002");
        check("同じコードは両方向で一致", first.equals(second) && second.equals(first), true);
        check("同じコードはハッシュ値も同じ", first.hashCode() == second.hashCode(), true);
        check("別コードは両方向で不一致", !first.equals(different) && !different.equals(first), true);
        check("nullとの比較", first.equals(null), false);
    }

    private static void check(String label, boolean actual, boolean expected) {
        if (actual != expected) {
            throw new AssertionError(label + " expected=" + expected + ", actual=" + actual);
        }
        System.out.printf("%s: %b%n", label, actual);
    }
}
