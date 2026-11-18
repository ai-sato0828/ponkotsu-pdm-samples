package example;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

public final class BeanCreationStageDemo {
    private BeanCreationStageDemo() {}

    public static void main(String[] args) {
        for (int retryLimit : List.of(0, 20)) {
            run("constructor", retryLimit);
            run("init-method", retryLimit);
        }
    }

    private static void run(String phase, int retryLimit) {
        System.out.printf("[%s retryLimit=%d]%n", phase, retryLimit);
        try (var context = prepare(phase, retryLimit)) {
            context.refresh();
            var rules = context.getBean("invoiceRules", InvoiceRules.class);
            System.out.printf("started invoiceRules retryLimit=%d%n", rules.retryLimit());
        } catch (BeanCreationException failure) {
            for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
                System.out.println(cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
            var frames = failure.getMostSpecificCause().getStackTrace();
            for (int i = 0; i < Math.min(2, frames.length); i++) {
                System.out.println("  at " + frames[i]);
            }
        }
    }

    static GenericXmlApplicationContext prepare(String phase, int retryLimit) {
        var context = new GenericXmlApplicationContext();
        var environment = context.getEnvironment();
        environment.getPropertySources().addFirst(
                new MapPropertySource("demo", Map.of("invoice.retry-limit", retryLimit)));

        var placeholders = new PropertySourcesPlaceholderConfigurer();
        placeholders.setEnvironment(environment);
        context.addBeanFactoryPostProcessor(placeholders);
        context.load("classpath:" + phase + ".xml");
        return context;
    }

    public interface InvoiceRules {
        int retryLimit();
    }

    public record ConstructorInvoiceRules(int retryLimit) implements InvoiceRules {
        public ConstructorInvoiceRules {
            validateRetryLimit(retryLimit);
        }
    }

    public record InitMethodInvoiceRules(int retryLimit) implements InvoiceRules {
        public void initialize() {
            validateRetryLimit(retryLimit);
        }
    }

    private static void validateRetryLimit(int retryLimit) {
        if (retryLimit < 1) {
            throw new IllegalArgumentException(
                    "invoice.retry-limit must be positive: " + retryLimit);
        }
    }
}
