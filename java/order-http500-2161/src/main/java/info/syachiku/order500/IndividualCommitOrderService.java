package info.syachiku.order500;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndividualCommitOrderService {
    private static final Logger log = LoggerFactory.getLogger(IndividualCommitOrderService.class);
    private final OrderRepository repository;
    private final NotificationStub notification;

    public IndividualCommitOrderService(OrderRepository repository, NotificationStub notification) {
        this.repository = repository;
        this.notification = notification;
    }

    public OrderView create(CreateOrderRequest request, String requestId) {
        OrderView order = repository.save(request.item(), requestId);
        log.info("operation=createOrder boundary=repository saveReturned=true requestId={}", requestId);
        notification.send(order, request.failNotification());
        log.info("operation=createOrder result=success requestId={}", requestId);
        return order;
    }
}
