package example.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShippingQuoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShippingQuoteApplication.class, args);
    }

    @Bean
    ShippingFee shippingFee() {
        return new ShippingFee();
    }
}
