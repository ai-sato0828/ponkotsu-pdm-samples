package info.syachiku.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

public final class SenderConfigurations {
    private SenderConfigurations() {
    }

    @Configuration(proxyBeanMethods = false)
    public static class WithoutDefault {
        @Bean
        @Qualifier("incident-channel")
        @Order(20)
        NotificationSender chatNotificationSender() {
            return message -> "CHAT:" + message;
        }

        @Bean
        @Order(10)
        NotificationSender mailNotificationSender() {
            return message -> "MAIL:" + message;
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class WithDefault {
        @Bean
        @Qualifier("incident-channel")
        @Order(20)
        NotificationSender chatNotificationSender() {
            return message -> "CHAT:" + message;
        }

        @Bean
        @Primary
        @Order(10)
        NotificationSender mailNotificationSender() {
            return message -> "MAIL:" + message;
        }
    }
}
