import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderFilterDemo {
    record Order(long id, boolean paid, boolean expired, int amount) {}
    record Exclusion(long orderId, String reason) {}
    record Selection(List<Order> targets, List<Exclusion> exclusions) {}

    static Stream<Order> eligibleOrders(List<Order> orders) {
        return orders.stream()
                .filter(Order::paid)
                .filter(Order::expired)
                .filter(order -> order.amount() >= 10_000);
    }

    static Selection selectWithFor(List<Order> orders) {
        List<Order> targets = new ArrayList<>();
        List<Exclusion> exclusions = new ArrayList<>();
        for (Order order : orders) {
            if (!order.paid()) {
                exclusions.add(new Exclusion(order.id(), "未入金"));
                continue;
            }
            if (!order.expired()) {
                exclusions.add(new Exclusion(order.id(), "期限内"));
                continue;
            }
            if (order.amount() < 10_000) {
                exclusions.add(new Exclusion(order.id(), "10,000円未満"));
                continue;
            }
            targets.add(order);
        }
        return new Selection(targets, exclusions);
    }

    public static void main(String[] args) {
        var orders = List.of(
                new Order(42, true, true, 10_000),
                new Order(72, false, true, 12_000),
                new Order(81, true, false, 12_000),
                new Order(61, true, true, 9_999),
                new Order(14, true, true, 12_000),
                new Order(95, false, false, 5_000)
        );

        List<Order> streamTargets = eligibleOrders(orders).toList();
        Selection selection = selectWithFor(orders);
        expect("対象注文のID", List.of(42L, 14L), ids(streamTargets));
        expect("抽出結果と順序", streamTargets, selection.targets());
        System.out.println("Stream・forの対象ID " + ids(streamTargets));

        expect("最初の除外理由", List.of(
                new Exclusion(72, "未入金"),
                new Exclusion(81, "期限内"),
                new Exclusion(61, "10,000円未満"),
                new Exclusion(95, "未入金")
        ), selection.exclusions());
        selection.exclusions().forEach(exclusion ->
                System.out.println("除外 " + exclusion.orderId() + " " + exclusion.reason()));

        expect("空の入力のStream", List.of(), eligibleOrders(List.of()).toList());
        Selection empty = selectWithFor(List.of());
        expect("空の入力のfor", new Selection(List.of(), List.of()), empty);
        System.out.println("空の入力 " + empty.targets());

        var extra = new Order(99, true, true, 10_000);
        try {
            streamTargets.add(extra);
            throw new AssertionError("Stream.toList()の結果へ追加できてしまった");
        } catch (UnsupportedOperationException expected) {
            System.out.println("Stream.toList()に追加するとUnsupportedOperationException");
        }
        List<Order> editableStreamTargets = eligibleOrders(orders)
                .collect(Collectors.toCollection(ArrayList::new));
        selection.targets().add(extra);
        editableStreamTargets.add(extra);
        expect("forで作った可変結果", List.of(42L, 14L, 99L), ids(selection.targets()));
        expect("Streamで作った可変結果", selection.targets(), editableStreamTargets);
        expect("元の入力", List.of(42L, 72L, 81L, 61L, 14L, 95L), ids(orders));
        System.out.println("両方を可変にした結果 " + ids(editableStreamTargets));
    }

    private static List<Long> ids(List<Order> orders) {
        return orders.stream().map(Order::id).toList();
    }

    private static void expect(String label, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected=" + expected + ", actual=" + actual);
        }
    }
}
