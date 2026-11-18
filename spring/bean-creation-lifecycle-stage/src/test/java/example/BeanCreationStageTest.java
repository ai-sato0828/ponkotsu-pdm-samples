package example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.BeanCreationException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanCreationStageTest {
    @Test
    void constructorFailureHasAnInstantiationException() {
        try (var context = BeanCreationStageDemo.prepare("constructor", 0)) {
            var outer = assertThrowsExactly(BeanCreationException.class, context::refresh);
            assertEquals("invoiceRules", outer.getBeanName());
            assertTrue(outer.getMessage().contains("Failed to instantiate"));

            var instantiation = assertInstanceOf(BeanInstantiationException.class, outer.getCause());
            assertTrue(instantiation.getMessage().endsWith("Constructor threw exception"));
            assertInvalidRetryLimit(instantiation.getCause(),
                    BeanCreationStageDemo.ConstructorInvoiceRules.class, "<init>");
        }
    }

    @Test
    void initMethodFailurePointsToInitialize() {
        try (var context = BeanCreationStageDemo.prepare("init-method", 0)) {
            var outer = assertThrowsExactly(BeanCreationException.class, context::refresh);
            assertEquals("invoiceRules", outer.getBeanName());
            assertTrue(outer.getMessage().endsWith("invoice.retry-limit must be positive: 0"));
            assertInvalidRetryLimit(outer.getCause(),
                    BeanCreationStageDemo.InitMethodInvoiceRules.class, "initialize");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"constructor", "init-method"})
    void bothConfigurationsStartWith20(String phase) {
        try (var context = BeanCreationStageDemo.prepare(phase, 20)) {
            context.refresh();
            assertArrayEquals(new String[]{"invoiceRules"}, context.getBeanDefinitionNames());
            assertEquals(20, context.getBean("invoiceRules", BeanCreationStageDemo.InvoiceRules.class).retryLimit());
        }
    }

    private static void assertInvalidRetryLimit(Throwable cause, Class<?> source, String method) {
        var invalid = assertInstanceOf(IllegalArgumentException.class, cause);
        assertEquals("invoice.retry-limit must be positive: 0", invalid.getMessage());
        assertEquals("validateRetryLimit", invalid.getStackTrace()[0].getMethodName());
        assertEquals(source.getName(), invalid.getStackTrace()[1].getClassName());
        assertEquals(method, invalid.getStackTrace()[1].getMethodName());
    }
}
