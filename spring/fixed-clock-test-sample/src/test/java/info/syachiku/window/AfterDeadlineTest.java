package info.syachiku.window;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfiguration.class)
@TestPropertySource(properties = "test.clock.instant=2026-11-20T08:00:00.000000001Z")
class AfterDeadlineTest {
    @Test
    void rejectsOneNanosecondAfter(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/application-window"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepting").value(false))
                .andExpect(jsonPath("$.closesAt").value("2026-11-20T08:00:00Z"));
    }
}
