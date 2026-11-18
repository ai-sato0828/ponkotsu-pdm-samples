package info.syachiku.paging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

abstract class InquiryPagingTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    RecordingSearch search;

    @BeforeEach
    void clearCalls() {
        search.calls = 0;
    }

    void assertAccepted(String query, int page, int size) throws Exception {
        mvc.perform(get("/inquiries" + query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size));
        assertEquals(1, search.calls);
    }

    void assertRejected(String query) throws Exception {
        mvc.perform(get("/inquiries" + query))
                .andExpect(status().isBadRequest());
        assertEquals(0, search.calls);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RecordingSearchConfig {
        @Bean
        @Primary
        RecordingSearch recordingSearch() {
            return new RecordingSearch();
        }
    }

    static class RecordingSearch extends InquirySearch {
        int calls;

        @Override
        PageSelection run(Pageable pageable) {
            calls++;
            return super.run(pageable);
        }
    }
}

@SpringBootTest(classes = {PageableComparisonApplication.class, InquiryPagingTest.RecordingSearchConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("strict")
class StrictInquiryPagingTest extends InquiryPagingTest {
    @ParameterizedTest
    @CsvSource({"'', 0, 20", "?page=1&size=2, 1, 2"})
    void validInputsReachSearch(String query, int page, int size) throws Exception {
        assertAccepted(query, page, size);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?page=-1&size=2", "?page=1&size=0", "?page=1&size=-2",
            "?page=1&size=51", "?page=1&size=2500"
    })
    void invalidInputsAreRejectedBeforeSearch(String query) throws Exception {
        assertRejected(query);
    }
}

@SpringBootTest(classes = {PageableComparisonApplication.class, InquiryPagingTest.RecordingSearchConfig.class})
@AutoConfigureMockMvc
class DefaultInquiryPagingTest extends InquiryPagingTest {
    @ParameterizedTest
    @CsvSource({
            "'', 0, 20", "?page=1&size=2, 1, 2", "?page=-1&size=2, 0, 2",
            "?page=1&size=0, 1, 20", "?page=1&size=-2, 1, 20",
            "?page=1&size=51, 1, 51", "?page=1&size=2500, 1, 2000"
    })
    void resolverSuppliesDefaultsAndBounds(String query, int page, int size) throws Exception {
        assertAccepted(query, page, size);
    }
}

@SpringBootTest(
        classes = {PageableComparisonApplication.class, InquiryPagingTest.RecordingSearchConfig.class},
        properties = "spring.data.web.pageable.max-page-size=50"
)
@AutoConfigureMockMvc
class CappedInquiryPagingTest extends InquiryPagingTest {
    @ParameterizedTest
    @CsvSource({
            "'', 0, 20", "?page=1&size=2, 1, 2", "?page=-1&size=2, 0, 2",
            "?page=1&size=0, 1, 20", "?page=1&size=-2, 1, 20",
            "?page=1&size=51, 1, 50", "?page=1&size=2500, 1, 50"
    })
    void configuredMaximumDoesNotTurnNormalizationIntoRejection(String query, int page, int size) throws Exception {
        assertAccepted(query, page, size);
    }
}
