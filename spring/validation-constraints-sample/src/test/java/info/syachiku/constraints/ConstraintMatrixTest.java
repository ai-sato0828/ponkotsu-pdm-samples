package info.syachiku.constraints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import org.hibernate.validator.HibernateValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConstraintMatrixTest {
    private static final Map<String, String> CONSTRAINT_NAMES = Map.of(
            "not-null", "NotNull", "not-empty", "NotEmpty",
            "not-blank", "NotBlank", "required-name", "RequiredName");

    @Autowired
    MockMvc mvc;

    static Stream<Arguments> inputCases() throws IOException {
        var arguments = new ArrayList<Arguments>();
        try (var input = ConstraintMatrixTest.class.getResourceAsStream("/input-cases.json")) {
            JsonNode cases = new ObjectMapper().readTree(input);
            for (JsonNode item : cases) {
                for (String endpoint : new String[]{"not-null", "not-empty", "not-blank", "required-name"}) {
                    arguments.add(Arguments.of(endpoint, item.path("id").asText(),
                            item.path("body").toString(), item.path("expected").path(endpoint).asInt()));
                }
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest(name = "{0} / {1} -> {3}")
    @MethodSource("inputCases")
    void 同じnameの入力を制約ごとの仕様と照合する(
            String endpoint, String caseId, String body, int expectedStatus) throws Exception {
        var response = mvc.perform(post("/validation/" + endpoint)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        if (expectedStatus == 400) {
            response.andExpect(jsonPath("$.errors.length()").value(1))
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].constraint").value(CONSTRAINT_NAMES.get(endpoint)))
                    .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,
                            result.getResolvedException()));
        } else {
            String original = new ObjectMapper().readTree(body).path("name").asText();
            response.andExpect(jsonPath("$.name").value(original));
        }
    }

    @Test
    void 記事のProvider版に固定されている() {
        assertEquals("8.0.3.Final", HibernateValidator.class.getPackage().getImplementationVersion());
    }

    @Test
    void 文字を含む氏名の前後と途中の空白は勝手に削らない() throws Exception {
        mvc.perform(post("/validation/required-name").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  山 田  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("  山 田  "));
    }
}
