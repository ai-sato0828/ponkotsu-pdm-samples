package info.syachiku.registration.payment;

public interface PaymentGateway {
    String authorize(String orderId);
}
