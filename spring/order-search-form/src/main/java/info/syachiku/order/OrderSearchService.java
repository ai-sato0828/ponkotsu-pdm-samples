package info.syachiku.order;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class OrderSearchService {
    private static final int PAGE_SIZE = 2;
    private final List<Order> orders = List.of(
            new Order(1, "Coffee", "OPEN", LocalDate.of(2026, 10, 1), "S01"),
            new Order(2, "Coffee", "OPEN", LocalDate.of(2026, 10, 2), "S02"),
            new Order(3, "Coffee", "CLOSED", LocalDate.of(2026, 10, 3), "S01"),
            new Order(4, "Tea", "OPEN", LocalDate.of(2026, 10, 4), "S01"),
            new Order(5, "Coffee", "OPEN", LocalDate.of(2026, 10, 5), "S01"));

    List<Order> search(OrderSearchCondition condition) {
        return orders.stream()
                .filter(order -> condition.keyword() == null || order.name().contains(condition.keyword()))
                .filter(order -> condition.status() == null || order.status().equals(condition.status()))
                .filter(order -> condition.fromDate() == null || !order.date().isBefore(condition.fromDate()))
                .filter(order -> condition.toDate() == null || !order.date().isAfter(condition.toDate()))
                .filter(order -> condition.staffId() == null || order.staffId().equals(condition.staffId()))
                .skip((condition.page() - 1L) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .toList();
    }
}
