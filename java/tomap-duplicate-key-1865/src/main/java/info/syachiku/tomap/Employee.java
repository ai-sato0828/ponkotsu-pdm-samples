package info.syachiku.tomap;

import java.util.Objects;

/** 社員番号を一意にする例。User.getId()の例とは別の型。 */
public record Employee(String employeeNo, String name) {
    public Employee {
        Objects.requireNonNull(employeeNo, "employeeNo");
        Objects.requireNonNull(name, "name");
    }
}
