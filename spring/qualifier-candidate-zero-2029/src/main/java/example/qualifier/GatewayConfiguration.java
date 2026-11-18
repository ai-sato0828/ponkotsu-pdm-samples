package example.qualifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class GatewayConfiguration {
    @Bean
    @Qualifier("card-gateway")
    PaymentGateway cardGateway() {
        return orderId -> "CARD:" + orderId;
    }

    @Bean
    @Qualifier("bank-gateway")
    PaymentGateway bankGateway() {
        return orderId -> "BANK:" + orderId;
    }
}
