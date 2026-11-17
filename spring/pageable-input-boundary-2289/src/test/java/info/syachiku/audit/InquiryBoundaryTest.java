package info.syachiku.audit;

import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InquiryBoundaryTest {
    @Autowired InquiryRepository repository;
    @Autowired MockMvc mvc;
    @Autowired EntityManagerFactory entityManagerFactory;
    private Statistics statistics;

    @BeforeEach
    void seedBeforeClearingStatistics() {
        repository.deleteAll();
        Instant time = Instant.parse("2026-11-01T00:00:00Z");
        repository.saveAll(List.of(
                new Inquiry(1L, "問い合わせ1", Status.OPEN, time),
                new Inquiry(2L, "問い合わせ2", Status.OPEN, time),
                new Inquiry(3L, "問い合わせ3", Status.OPEN, time),
                new Inquiry(4L, "完了済み", Status.CLOSED, time)));
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        assertTrue(statistics.isStatisticsEnabled());
        statistics.clear();
    }

    @ParameterizedTest(name = "reject before DB: page={0}, size={1}")
    @CsvSource({"-1,2", "0,0", "0,51", "42949673,50", "2147483647,50"})
    void rejectsInvalidBoundsWithoutPreparingSql(int page, int size) throws Exception {
        mvc.perform(get("/inquiries").param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest());
        assertEquals(0L, statistics.getPrepareStatementCount());
        System.out.printf("REJECT page=%d size=%d status=400 sql=%d%n",
                page, size, statistics.getPrepareStatementCount());
    }

    @ParameterizedTest(name = "int conversion: page={0}, size={1}")
    @CsvSource({"2147483648,1", "0,2147483648"})
    void intOverflowTextFailsBeforeControllerAndDatabase(String page, String size)
            throws Exception {
        var result = mvc.perform(get("/inquiries").param("page", page).param("size", size))
                .andExpect(status().isBadRequest()).andReturn();
        assertInstanceOf(MethodArgumentTypeMismatchException.class, result.getResolvedException());
        assertEquals(0L, statistics.getPrepareStatementCount());
    }

    @ParameterizedTest(name = "accepted large offset: page={0}, size={1}")
    @CsvSource({"42949672,50,1", "2147483647,1,3"})
    void acceptsRepresentableOffsetsAndReportsAnEmptyPage(int page, int size, int pages)
            throws Exception {
        mvc.perform(get("/inquiries").param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(pages))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @ParameterizedTest(name = "size endpoint: {0}")
    @CsvSource({"1,1,3,true", "50,3,1,false"})
    void acceptsBothSizeEndpoints(int size, int items, int pages, boolean next)
            throws Exception {
        mvc.perform(get("/inquiries").param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(items))
                .andExpect(jsonPath("$.items[0].id").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(pages))
                .andExpect(jsonPath("$.hasNext").value(next));
    }

    @Test
    void omissionUsesPageZeroAndSizeTwenty() throws Exception {
        mvc.perform(get("/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].id").value(3))
                .andExpect(jsonPath("$.items[1].id").value(2))
                .andExpect(jsonPath("$.items[2].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void emptyDatabaseStillReturnsAValidEmptyPage() throws Exception {
        repository.deleteAll();
        statistics.clear();
        mvc.perform(get("/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));
    }
}
