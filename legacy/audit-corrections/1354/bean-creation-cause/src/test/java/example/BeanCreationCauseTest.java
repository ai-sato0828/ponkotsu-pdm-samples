package example;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import static org.junit.jupiter.api.Assertions.*;

class BeanCreationCauseTest {
    @Test
    void findsTheFailedBeanAndInvalidValue() {
        try (var context = createContext(0)) {
            var outer = assertThrows(UnsatisfiedDependencyException.class, context::refresh);
            assertEquals("invoiceJob", outer.getBeanName());

            var failed = assertInstanceOf(BeanCreationException.class, outer.getCause());
            assertEquals("invoiceRules", failed.getBeanName());

            var root = assertInstanceOf(IllegalArgumentException.class, failed.getCause());
            assertEquals("invoice.batch-size must be positive: 0", root.getMessage());
            assertEquals("initialize", root.getStackTrace()[0].getMethodName());
            outer.printStackTrace(System.out);
        }
    }

    @Test
    void startsWhenTheSamePropertyHasAValidValue() {
        try (var context = createContext(20)) {
            context.refresh();
            assertEquals(20, context.getBean(InvoiceJob.class).rules().batchSize());
        }
    }

    private AnnotationConfigApplicationContext createContext(int batchSize) {
        var context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("example", Map.of("invoice.batch-size", batchSize)));
        context.register(Config.class);
        return context;
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {
        @Bean
        InvoiceJob invoiceJob(InvoiceRules rules) {
            return new InvoiceJob(rules);
        }

        @Bean(initMethod = "initialize")
        InvoiceRules invoiceRules(@Value("${invoice.batch-size}") int batchSize) {
            return new InvoiceRules(batchSize);
        }
    }

    record InvoiceJob(InvoiceRules rules) {}

    record InvoiceRules(int batchSize) {
        public void initialize() {
            if (batchSize < 1) {
                throw new IllegalArgumentException(
                        "invoice.batch-size must be positive: " + batchSize);
            }
        }
    }
}
