package info.syachiku.registration.orders;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderCsvParser parser;
    private final OrderRepository orders;
    private final OrderQueryRepository queries;

    public OrderService(OrderCsvParser parser, OrderRepository orders, OrderQueryRepository queries) {
        this.parser = parser;
        this.orders = orders;
        this.queries = queries;
    }

    @Transactional
    public long place(String orderLine) {
        var row = parser.parse(orderLine);
        return orders.save(new OrderRecord(row)).getId();
    }

    @Transactional(readOnly = true)
    public String findStatus(long id) {
        return queries.findStatus(id);
    }
}
