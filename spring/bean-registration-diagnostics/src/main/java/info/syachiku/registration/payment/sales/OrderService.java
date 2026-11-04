package info.syachiku.registration.payment.sales;

import info.syachiku.registration.payment.PaymentGateway;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String authorize(String orderId) {
        return paymentGateway.authorize(orderId);
    }
}
