package info.syachiku.window;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
class FixedClockConfiguration {
    @Bean
    @Primary
    Clock fixedTestClock(@Value("${test.clock.instant}") String now) {
        return Clock.fixed(Instant.parse(now), ZoneOffset.UTC);
    }
}
