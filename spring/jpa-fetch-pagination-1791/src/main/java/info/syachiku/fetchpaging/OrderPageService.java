package info.syachiku.fetchpaging;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OrderPageService {
    private final OrderRepository orders;

    OrderPageService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public List<OrderRow> findPage(int page, int size) {
        List<Long> ids = orders.findPageIds(PageRequest.of(page, size));
        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, Order> byId = orders.findWithItems(ids).stream()
                .collect(Collectors.toMap(Order::id, Function.identity()));
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(order -> new OrderRow(order.id(), order.itemSkus()))
                .toList();
    }

    record OrderRow(long id, List<String> itemSkus) {
    }
}
