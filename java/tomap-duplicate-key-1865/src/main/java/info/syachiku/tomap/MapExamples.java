package info.syachiku.tomap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MapExamples {
    private MapExamples() {
    }

    public static Map<String, Integer> lengthsWithoutMerge(List<String> values) {
        return Objects.requireNonNull(values, "values").stream()
                .collect(Collectors.toMap(value -> value, String::length));
    }

    public static Map<String, Integer> quantitiesWithoutMerge(List<OrderLine> lines) {
        return Objects.requireNonNull(lines, "lines").stream()
                .collect(Collectors.toMap(OrderLine::productCode, OrderLine::quantity));
    }

    public static Map<String, Integer> firstQuantities(List<OrderLine> lines) {
        return Objects.requireNonNull(lines, "lines").stream()
                .collect(Collectors.toMap(
                        OrderLine::productCode,
                        OrderLine::quantity,
                        (existing, incoming) -> existing));
    }

    public static Map<String, Integer> lastQuantities(List<OrderLine> lines) {
        return Objects.requireNonNull(lines, "lines").stream()
                .collect(Collectors.toMap(
                        OrderLine::productCode,
                        OrderLine::quantity,
                        (existing, incoming) -> incoming));
    }

    public static Map<String, Integer> totalQuantities(List<OrderLine> lines) {
        return Objects.requireNonNull(lines, "lines").stream()
                .collect(Collectors.toMap(
                        OrderLine::productCode,
                        OrderLine::quantity,
                        Integer::sum));
    }

    public static Map<String, List<OrderLine>> groupLines(List<OrderLine> lines) {
        return Objects.requireNonNull(lines, "lines").stream()
                .collect(Collectors.groupingBy(
                        OrderLine::productCode,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    public static Map<Long, String> namesWithoutMerge(List<User> users) {
        return Objects.requireNonNull(users, "users").stream()
                .collect(Collectors.toMap(User::getId, User::getName));
    }

    public static Map<Long, String> firstNamesById(List<User> users) {
        return Objects.requireNonNull(users, "users").stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getName,
                        (existing, incoming) -> existing));
    }

    public static Map<String, Employee> toEmployeeMap(List<Employee> employees) {
        return Objects.requireNonNull(employees, "employees").stream()
                .collect(Collectors.toMap(
                        Employee::employeeNo,
                        Function.identity(),
                        (existing, incoming) -> {
                            throw new IllegalStateException(
                                    "社員番号が重複しています: " + existing.employeeNo());
                        }));
    }

    public static Map<String, List<Order>> ordersByCustomer(List<Order> orders) {
        return Objects.requireNonNull(orders, "orders").stream()
                .collect(Collectors.groupingBy(Order::customerId));
    }

    public static Map<String, Long> countByStatus(List<Order> orders) {
        return Objects.requireNonNull(orders, "orders").stream()
                .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
    }

    public static Map<String, String> labelsInEncounterOrder(List<Item> items) {
        return Objects.requireNonNull(items, "items").stream()
                .collect(Collectors.toMap(
                        Item::code,
                        Item::label,
                        (existing, incoming) -> existing,
                        LinkedHashMap::new));
    }
}
