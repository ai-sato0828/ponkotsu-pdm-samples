package info.syachiku.notification;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationSelectionTest {
    @Test
    void 候補が二つで標準がなければ一つの注入先を決められない() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(SenderConfigurations.WithoutDefault.class, OrderService.class);

            var failure = assertThrows(UnsatisfiedDependencyException.class, context::refresh);
            var ambiguous = assertInstanceOf(NoUniqueBeanDefinitionException.class,
                    failure.getMostSpecificCause());
            assertEquals(Set.of("mailNotificationSender", "chatNotificationSender"),
                    Set.copyOf(ambiguous.getBeanNamesFound()));
        }
    }

    @Test
    void 型だけで注入した注文通知にはPrimaryのメールを選ぶ() {
        try (var context = new AnnotationConfigApplicationContext(
                SenderConfigurations.WithDefault.class, OrderService.class)) {

            assertEquals(2, context.getBeansOfType(NotificationSender.class).size());
            assertEquals("MAIL:order-42", context.getBean(OrderService.class).notifyOrder("order-42"));
        }
    }

    @Test
    void メールがPrimaryでもQualifierで障害通知をチャットへ送る() {
        try (var context = new AnnotationConfigApplicationContext(
                SenderConfigurations.WithDefault.class, FailureAlertService.class)) {

            assertEquals("CHAT:incident-7",
                    context.getBean(FailureAlertService.class).notifyFailure("incident-7"));
        }
    }

    @Test
    void ListではPrimaryだけに絞らずOrderで指定した順に両方受け取る() {
        try (var context = new AnnotationConfigApplicationContext(
                SenderConfigurations.WithDefault.class, BroadcastService.class)) {

            assertEquals(List.of("MAIL:maintenance", "CHAT:maintenance"),
                    context.getBean(BroadcastService.class).broadcast("maintenance"));
        }
    }
}
