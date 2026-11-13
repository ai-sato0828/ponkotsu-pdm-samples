package info.syachiku.order500;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionalOrderService {
    private static final Logger log = LoggerFactory.getLogger(TransactionalOrderService.class);
    private final OrderRepository repository;
    private final NotificationStub notification;

    public TransactionalOrderService(OrderRepository repository, NotificationStub notification) {
        this.repository = repository;
        this.notification = notification;
    }

    @Transactional
    public OrderView create(CreateOrderRequest request, String requestId) {
        OrderView order = repository.save(request.item(), requestId);
        log.info("operation=createOrder boundary=service saveReturned=true commitNotYet=true requestId={}", requestId);
        notification.send(order, request.failNotification());
        log.info("operation=createOrder result=success requestId={}", requestId);
        return order;
    }
}
