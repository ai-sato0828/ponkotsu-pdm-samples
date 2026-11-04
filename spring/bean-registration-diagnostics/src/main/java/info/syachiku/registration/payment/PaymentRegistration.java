package info.syachiku.registration.payment;

import info.syachiku.registration.payment.integration.CardPaymentGateway;
import info.syachiku.registration.payment.library.LibraryPaymentClient;
import info.syachiku.registration.payment.sales.OrderService;
import org.springframework.context.annotation.*;

public final class PaymentRegistration {
    private PaymentRegistration() {
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = OrderService.class)
    public static class SalesOnly {
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = {OrderService.class, CardPaymentGateway.class})
    public static class SalesAndIntegration {
    }

    @Configuration(proxyBeanMethods = false)
    public static class ExternalLibrary {
        @Bean
        LibraryPaymentClient libraryPaymentClient() {
            return new LibraryPaymentClient();
        }

        @Bean
        PaymentGateway libraryPaymentGateway(LibraryPaymentClient client) {
            return client::requestAuthorization;
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class ProfileGateway {
        @Bean
        @Profile("test-payment")
        PaymentGateway paymentGateway() {
            return orderId -> "STUB:" + orderId;
        }
    }
}
