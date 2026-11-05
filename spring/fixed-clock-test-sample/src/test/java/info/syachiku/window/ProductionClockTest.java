package info.syachiku.window;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ProductionClockTest {
    @Test
    void applicationWithoutTheTestImportUsesOnlyTheUtcClock(@Autowired ApplicationContext context) {
        assertEquals(Set.of("applicationClock"), context.getBeansOfType(Clock.class).keySet());
        assertEquals(ZoneOffset.UTC, context.getBean(Clock.class).getZone());
    }
}
