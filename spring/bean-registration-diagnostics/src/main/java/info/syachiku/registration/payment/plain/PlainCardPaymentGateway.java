package info.syachiku.registration.payment.plain;

import info.syachiku.registration.payment.PaymentGateway;

public class PlainCardPaymentGateway implements PaymentGateway {
    @Override
    public String authorize(String orderId) {
        return "PLAIN:" + orderId;
    }
}
