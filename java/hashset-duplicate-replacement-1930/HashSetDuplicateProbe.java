import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class HashSetDuplicateProbe {
    public static void main(String[] args) {
        Employee original = new Employee("E001", "田中");
        Employee incoming = new Employee("E001", "佐藤");
        check(original != incoming, "The inputs must be different objects");
        check(original.equals(incoming), "Employee identity is the employee ID");
        check(original.hashCode() == incoming.hashCode(), "Equal IDs need equal hashes");

        Set<Employee> employees = new HashSet<>();
        check(employees.add(original), "The first employee must be added");
        boolean added = employees.add(incoming);
        Employee kept = employees.iterator().next();
        check(!added, "An equal employee must not be added");
        check(employees.size() == 1, "The set must still contain one employee");
        check(kept == original, "HashSet must retain the original object in this example");
        check(kept.name.equals("田中"), "HashSet.add must not update the employee name");
        System.out.printf("HashSet.add: id=%s, result=%s, size=%d, keptName=%s, sameAsOriginal=%s%n",
                incoming.id, added, employees.size(), kept.name, kept == original);

        Map<String, Employee> employeeById = new HashMap<>();
        check(employeeById.put(original.id, original) == null, "The first mapping has no previous value");
        Employee previous = employeeById.put(incoming.id, incoming);
        Employee current = employeeById.get("E001");
        check(previous == original, "Map.put must return the previous employee");
        check(employeeById.size() == 1, "The map must still contain one employee ID");
        check(current == incoming, "Map.put must store the incoming employee");
        check(current.name.equals("佐藤"), "The replacement must contain the incoming name");
        System.out.printf("Map.put: id=%s, previousName=%s, size=%d, currentName=%s, sameAsIncoming=%s%n",
                incoming.id, previous.name, employeeById.size(), current.name, current == incoming);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class Employee {
        private final String id;
        private final String name;

        private Employee(String id, String name) {
            this.id = Objects.requireNonNull(id);
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Employee employee && id.equals(employee.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
