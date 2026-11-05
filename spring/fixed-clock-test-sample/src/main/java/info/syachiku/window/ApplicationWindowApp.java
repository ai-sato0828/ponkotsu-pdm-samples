package info.syachiku.window;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(proxyBeanMethods = false)
public class ApplicationWindowApp {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationWindowApp.class, args);
    }

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}
