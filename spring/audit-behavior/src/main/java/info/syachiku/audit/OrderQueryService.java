package info.syachiku.audit;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {
    private final OrderRepository repository;

    OrderQueryService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OrderRow> findRows() {
        return rows(repository.findAll(Sort.by("id")));
    }

    @Transactional(readOnly = true)
    public List<OrderRow> findRowsWithFetchJoin() {
        return rows(repository.findAllWithCustomer());
    }

    @Transactional(readOnly = true)
    public List<OrderRow> findRowsWithEntityGraph() {
        return rows(repository.findByStatusOrderById(OrderStatus.WAITING));
    }

    @Transactional(readOnly = true)
    public List<OrderRow> findRowsWithProjection() {
        return repository.findOrderRows();
    }

    private List<OrderRow> rows(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderRow(order.getId(), order.getCustomer().getName(), order.getTotalAmount()))
                .toList();
    }
}
