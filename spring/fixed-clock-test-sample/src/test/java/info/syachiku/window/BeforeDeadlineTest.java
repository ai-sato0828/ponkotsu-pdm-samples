package info.syachiku.window;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfiguration.class)
@TestPropertySource(properties = "test.clock.instant=2026-11-20T07:59:59.999999999Z")
class BeforeDeadlineTest {
    @Test
    void acceptsOneNanosecondBefore(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/application-window"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepting").value(true))
                .andExpect(jsonPath("$.closesAt").value("2026-11-20T08:00:00Z"));
    }

    @Test
    void fixedClockIsPrimaryAndProductionBeanKeepsItsName(@Autowired ApplicationContext context) {
        assertEquals(Set.of("applicationClock", "fixedTestClock"),
                context.getBeansOfType(Clock.class).keySet());
        Clock fixed = context.getBean("fixedTestClock", Clock.class);
        assertSame(fixed, context.getBean(Clock.class));
        assertEquals(Instant.parse("2026-11-20T07:59:59.999999999Z"), fixed.instant());
    }
}
