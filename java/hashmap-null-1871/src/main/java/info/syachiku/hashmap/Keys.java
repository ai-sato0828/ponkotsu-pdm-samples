package info.syachiku.hashmap;

import java.util.Objects;

public final class Keys {
    private Keys() {
    }

    // 意図的な不良例: equals/hashCodeを実装せず、インスタンス同一性で比較する。
    public static final class IdentityKey {
        private final String employeeNo;

        public IdentityKey(String employeeNo) {
            this.employeeNo = Objects.requireNonNull(employeeNo, "employeeNo");
        }
    }

    // 意図的な不良例: equals/hashCodeに使う値をput後に変更できる。
    public static final class MutableKey {
        private String employeeNo;

        public MutableKey(String employeeNo) {
            setEmployeeNo(employeeNo);
        }

        public void setEmployeeNo(String employeeNo) {
            this.employeeNo = Objects.requireNonNull(employeeNo, "employeeNo");
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MutableKey key
                    && employeeNo.equals(key.employeeNo);
        }

        @Override
        public int hashCode() {
            return employeeNo.hashCode();
        }
    }

    // 修正版: 等価性に使う社員番号を変更できない。
    public record EmployeeKey(String employeeNo) {
        public EmployeeKey {
            Objects.requireNonNull(employeeNo, "employeeNo");
        }
    }
}
