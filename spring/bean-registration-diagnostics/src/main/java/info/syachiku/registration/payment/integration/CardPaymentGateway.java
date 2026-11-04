package info.syachiku.registration.payment.integration;

import info.syachiku.registration.payment.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentGateway implements PaymentGateway {
    @Override
    public String authorize(String orderId) {
        return "CARD:" + orderId;
    }
}
