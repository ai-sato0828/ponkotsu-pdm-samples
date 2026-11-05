package example.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

class StatusConversionTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new StatusController()).build();

    @Test
    void javaValueOfRequiresAnExactConstantName() {
        assertEquals(Status.ACTIVE, Status.valueOf("ACTIVE"));
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("active"));
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("ACTIVE "));
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf(""));
        assertThrows(NullPointerException.class, () -> Status.valueOf(null));
    }

    @ParameterizedTest
    @CsvSource({"active, ACTIVE", "inactive, INACTIVE"})
    void externalValuesAreMappedByContract(String input, Status expected) {
        assertEquals(expected, Status.fromValue(input).orElseThrow());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"unknown", "ACTIVE", " active "})
    void unsupportedExternalValuesAreRejected(String input) {
        assertTrue(Status.fromValue(input).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "ACTIVE "})
    void defaultMvcAcceptsUppercaseEvenWithTrailingSpace(String input) throws Exception {
        mvc.perform(get("/orders").param("status", input))
                .andExpect(status().isOk())
                .andExpect(content().string("ACTIVE"));
    }

    @Test
    void defaultMvcReportsTypeMismatchForAnUnmatchedName() throws Exception {
        mvc.perform(get("/orders").param("status", "active"))
                .andExpect(status().isBadRequest())
                .andExpect(r -> assertInstanceOf(
                        MethodArgumentTypeMismatchException.class, r.getResolvedException()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void defaultMvcTreatsEmptyAndBlankValuesAsMissingAfterConversion(String input) throws Exception {
        mvc.perform(get("/orders").param("status", input))
                .andExpect(status().isBadRequest())
                .andExpect(r -> assertTrue(assertInstanceOf(
                        MissingServletRequestParameterException.class,
                        r.getResolvedException()).isMissingAfterConversion()));
    }

    @Test
    void externalMvcAcceptsTheDocumentedLowercaseValue() throws Exception {
        mvc.perform(get("/orders/external").param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(content().string("ACTIVE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACTIVE", "ACTIVE ", " active ", "unknown"})
    void externalMvcRejectsUndocumentedValues(String input) throws Exception {
        mvc.perform(get("/orders/external").param("status", input))
                .andExpect(status().isBadRequest())
                .andExpect(r -> assertInstanceOf(
                        ResponseStatusException.class, r.getResolvedException()));
    }
}
