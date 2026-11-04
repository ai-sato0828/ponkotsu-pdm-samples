package example.orderquote;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderQuoteHttpTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void returns200WithTheTotal() throws Exception {
        mvc.perform(post("/order-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(example("valid.json")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2750));
    }

    @Test
    void acceptsAnExplicitZeroPrice() throws Exception {
        mvc.perform(post("/order-quotes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"lines":[{"quantity":1,"unitPrice":0}]}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void returns400ForAMissingPrice() throws Exception {
        mvc.perform(post("/order-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(example("missing-price.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BODY"));
    }

    @Test
    void returns400ForQuantityOutsideTheBusinessRange() throws Exception {
        mvc.perform(post("/order-quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(example("invalid-quantity.json")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_QUOTE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{",
            "{\"lines\":[{\"quantity\":\"1\",\"unitPrice\":350}]}",
            "{\"lines\":[{\"quantity\":1.5,\"unitPrice\":350}]}"
    })
    void returns400ForJsonThatCannotBeReadAsIntegerLines(String json) throws Exception {
        mvc.perform(post("/order-quotes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_JSON"));
    }

    private String example(String name) throws Exception {
        return Files.readString(Path.of("requests", name));
    }
}
