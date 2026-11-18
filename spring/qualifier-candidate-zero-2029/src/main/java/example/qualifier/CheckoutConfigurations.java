package example.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

final class CheckoutConfigurations {
    private CheckoutConfigurations() {
    }

    @Configuration(proxyBeanMethods = false)
    static class WrongLabel {
        @Bean
        CheckoutService checkoutService(@Qualifier("card-gateawy") PaymentGateway gateway) {
            return new CheckoutService(gateway);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CorrectLabel {
        @Bean
        CheckoutService checkoutService(@Qualifier("card-gateway") PaymentGateway gateway) {
            return new CheckoutService(gateway);
        }
    }
}
