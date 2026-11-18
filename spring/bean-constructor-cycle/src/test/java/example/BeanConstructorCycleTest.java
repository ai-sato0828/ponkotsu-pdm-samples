package example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;

import static example.BeanConstructorCycleDemo.*;
import static org.junit.jupiter.api.Assertions.*;

class BeanConstructorCycleTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void constructorCycleFailsDuringRefreshForBothSettings(boolean allowCircularReferences) {
        try (var context = prepareCycle(allowCircularReferences)) {
            var top = assertThrowsExactly(UnsatisfiedDependencyException.class, context::refresh);
            assertEquals("orderService", top.getBeanName());
            var next = assertInstanceOf(UnsatisfiedDependencyException.class, top.getCause());
            assertEquals("invoiceService", next.getBeanName());
            var deepest = assertInstanceOf(BeanCurrentlyInCreationException.class,
                    top.getMostSpecificCause());
            assertEquals("orderService", deepest.getBeanName());
            assertFalse(context.isActive());
        }
    }

    @Test
    void workflowStartsAndIssuesInvoice() {
        try (var context = prepareWorkflow()) {
            context.refresh();
            var workflow = context.getBean(CheckoutWorkflow.class);
            assertEquals(new Invoice("ORDER-42", 1800), workflow.issueInvoice("ORDER-42"));
            assertTrue(context.isActive());
        }
    }
}
