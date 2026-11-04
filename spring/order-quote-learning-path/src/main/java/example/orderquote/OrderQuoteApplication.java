package example.orderquote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrderQuoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderQuoteApplication.class, args);
    }

    @Bean
    QuoteCalculator quoteCalculator() {
        return new QuoteCalculator();
    }
}
