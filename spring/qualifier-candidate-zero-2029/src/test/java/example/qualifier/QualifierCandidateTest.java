package example.qualifier;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualifierCandidateTest {
    private static final Set<String> GATEWAY_NAMES = Set.of("cardGateway", "bankGateway");

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(GatewayConfiguration.class);

    @Test
    void configurationRegistersTwoBeansOfTheSameType() {
        runner.run(context -> {
            assertNull(context.getStartupFailure());
            var names = context.getBeansOfType(PaymentGateway.class).keySet();
            assertEquals(GATEWAY_NAMES, names);
            System.out.println("REGISTERED PaymentGateway names=" + names.stream().sorted().toList());
        });
    }

    @Test
    void misspelledQualifierLeavesNoCandidateForTheInjectionPoint() {
        runner.withUserConfiguration(CheckoutConfigurations.WrongLabel.class).run(context -> {
            var failure = assertInstanceOf(
                    UnsatisfiedDependencyException.class, context.getStartupFailure());
            assertEquals("checkoutService", failure.getBeanName());
            Throwable cause = failure.getMostSpecificCause();
            assertEquals(NoSuchBeanDefinitionException.class, cause.getClass());
            assertTrue(cause.getMessage().contains("Dependency annotations:"));
            assertTrue(cause.getMessage().contains("card-gateawy"));

            System.out.println("WRONG top=" + failure.getClass().getSimpleName()
                    + " bean=" + failure.getBeanName());
            System.out.println("Caused by: " + cause);
        });
    }

    @Test
    void correctQualifierChoosesCardWithoutChangingRegistration() {
        runner.withUserConfiguration(CheckoutConfigurations.CorrectLabel.class).run(context -> {
            assertNull(context.getStartupFailure());
            assertEquals(GATEWAY_NAMES, context.getBeansOfType(PaymentGateway.class).keySet());
            String authorization = context.getBean(CheckoutService.class).authorize("42");
            assertEquals("CARD:42", authorization);
            System.out.println("FIXED qualifier=card-gateway result=" + authorization);
        });
    }
}
