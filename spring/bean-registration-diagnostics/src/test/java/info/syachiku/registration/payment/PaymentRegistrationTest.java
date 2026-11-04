package info.syachiku.registration.payment;

import info.syachiku.registration.payment.integration.CardPaymentGateway;
import info.syachiku.registration.payment.library.LibraryPaymentClient;
import info.syachiku.registration.payment.plain.PlainCardPaymentGateway;
import info.syachiku.registration.payment.sales.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRegistrationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner();

    @Test
    void 自作クラスをスキャンしても登録の目印がなければ候補は零件() {
        runner.withUserConfiguration(PaymentRegistration.SalesOnly.class, PlainScan.class)
                .run(context -> assertMissingGateway(context.getStartupFailure()));
    }

    @Test
    void 実装にComponentがあってもスキャン外なら候補は零件() {
        runner.withUserConfiguration(PaymentRegistration.SalesOnly.class)
                .run(context -> assertMissingGateway(context.getStartupFailure()));

        runner.withUserConfiguration(PaymentRegistration.SalesAndIntegration.class)
                .run(context -> {
                    assertThat(context.getBeansOfType(PaymentGateway.class)).hasSize(1);
                    assertThat(context.getBean(PaymentGateway.class)).isInstanceOf(CardPaymentGateway.class);
                    assertThat(context.getBean(OrderService.class).authorize("42")).isEqualTo("CARD:42");
                });
    }

    @Test
    void 外部ライブラリのクラスはスキャンだけでは登録されずBeanメソッドで構成する() {
        runner.withUserConfiguration(LibraryScan.class)
                .run(context -> assertThat(context.getBeansOfType(LibraryPaymentClient.class)).isEmpty());

        runner.withUserConfiguration(PaymentRegistration.SalesOnly.class,
                        PaymentRegistration.ExternalLibrary.class)
                .run(context -> {
                    assertThat(context.getBeansOfType(LibraryPaymentClient.class)).hasSize(1);
                    assertThat(context.getBean(OrderService.class).authorize("42")).isEqualTo("SDK:42");
                });
    }

    @Test
    void Profileが不一致なら候補は零件で有効にすると登録される() {
        var setup = runner.withUserConfiguration(PaymentRegistration.SalesOnly.class,
                PaymentRegistration.ProfileGateway.class);
        setup.run(context -> assertMissingGateway(context.getStartupFailure()));

        setup.withPropertyValues("spring.profiles.active=test-payment")
                .run(context -> assertThat(context.getBean(OrderService.class).authorize("42"))
                        .isEqualTo("STUB:42"));
    }

    private void assertMissingGateway(Throwable startupFailure) {
        assertThat(startupFailure).isNotNull();
        assertThat(startupFailure).hasRootCauseInstanceOf(NoSuchBeanDefinitionException.class);
        var cause = (NoSuchBeanDefinitionException) startupFailure.getCause();
        assertThat(cause.getBeanType()).isEqualTo(PaymentGateway.class);
        assertThat(cause.getNumberOfBeansFound()).isZero();
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = PlainCardPaymentGateway.class)
    static class PlainScan {
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = LibraryPaymentClient.class)
    static class LibraryScan {
    }
}
