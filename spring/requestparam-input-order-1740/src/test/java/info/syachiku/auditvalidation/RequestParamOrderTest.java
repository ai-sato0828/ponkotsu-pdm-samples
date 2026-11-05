package info.syachiku.auditvalidation;

import java.net.URI;
import java.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestParamOrderTest {
    private static final String URL = "http://localhost:8174/api/employees";

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @CsvSource({"''", "?page=abc", "?page=-1"})
    void missingKeywordStopsArgumentResolution(String query) throws Exception {
        var result = mvc.perform(get(URI.create(URL + query))).andExpect(status().isBadRequest()).andReturn();
        var failure = assertInstanceOf(MissingServletRequestParameterException.class, result.getResolvedException());
        assertEquals("keyword", failure.getParameterName());
        print(query, 400, failure.getClass().getSimpleName(), failure.getParameterName());
    }

    @ParameterizedTest
    @CsvSource({"?keyword=A&page=abc", "?keyword=%20&page=abc"})
    void invalidPageStopsBeforeMethodValidation(String query) throws Exception {
        var result = mvc.perform(get(URI.create(URL + query))).andExpect(status().isBadRequest()).andReturn();
        var failure = assertInstanceOf(MethodArgumentTypeMismatchException.class, result.getResolvedException());
        assertEquals("page", failure.getName());
        print(query, 400, failure.getClass().getSimpleName(), failure.getName());
    }

    @ParameterizedTest
    @CsvSource({"?keyword=A&page=-1, page, Min", "?keyword=%20&page=0, keyword, NotBlank"})
    void constraintsRunAfterArgumentsAreResolved(String query, String field, String rule) throws Exception {
        var result = mvc.perform(get(URI.create(URL + query))).andExpect(status().isBadRequest()).andReturn();
        var failure = assertInstanceOf(HandlerMethodValidationException.class, result.getResolvedException());
        var violations = failure.getParameterValidationResults();
        assertEquals(1, violations.size());
        assertEquals(field, violations.get(0).getMethodParameter().getParameterName());
        assertTrue(Arrays.asList(violations.get(0).getResolvableErrors().get(0).getCodes()).contains(rule));
        print(query, 400, failure.getClass().getSimpleName(), field + " " + rule);
    }

    @ParameterizedTest
    @CsvSource({"?keyword=A", "?keyword=A&page=", "?keyword=A&page=0"})
    void omittedOrEmptyPageUsesZero(String query) throws Exception {
        mvc.perform(get(URI.create(URL + query))).andExpect(status().isOk()).andExpect(content().string("A:0"));
        print(query, 200, "-", "A:0");
    }

    private static void print(String query, int statusCode, String exception, String detail) {
        System.out.printf("CHECK %s | %d | %s | %s%n", URL + query, statusCode, exception, detail);
    }
}
