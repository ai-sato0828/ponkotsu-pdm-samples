package info.syachiku.notification;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final NotificationSender notificationSender;

    public OrderService(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public String notifyOrder(String orderId) {
        return notificationSender.send(orderId);
    }
}
