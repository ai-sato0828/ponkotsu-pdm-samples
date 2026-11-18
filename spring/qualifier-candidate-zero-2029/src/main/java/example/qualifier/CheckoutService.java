package example.qualifier;

final class CheckoutService {
    private final PaymentGateway gateway;

    CheckoutService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    String authorize(String orderId) {
        return gateway.authorize(orderId);
    }
}
