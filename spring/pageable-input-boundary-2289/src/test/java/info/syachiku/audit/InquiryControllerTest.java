package info.syachiku.audit;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InquiryControllerTest {
    @Autowired
    private InquiryRepository repository;
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        Instant time = Instant.parse("2026-11-01T00:00:00Z");
        repository.saveAll(List.of(
                new Inquiry(1L, "問い合わせ1", Status.OPEN, time),
                new Inquiry(2L, "問い合わせ2", Status.OPEN, time),
                new Inquiry(3L, "問い合わせ3", Status.OPEN, time),
                new Inquiry(4L, "完了済み", Status.CLOSED, time)));
    }

    @Test
    void 同時刻のOPENをID降順で返し最終ページと範囲外を区別する() throws Exception {
        mvc.perform(get("/inquiries").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(3))
                .andExpect(jsonPath("$.items[1].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true));

        mvc.perform(get("/inquiries").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        mvc.perform(get("/inquiries").param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 取得件数が51なら400を返す() throws Exception {
        mvc.perform(get("/inquiries").param("size", "51"))
                .andExpect(status().isBadRequest());
    }
}
