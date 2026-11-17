package info.syachiku.mediatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderPreviewHttpTest {
    @Autowired MockMvc mvc;

    @ParameterizedTest(name = "POST JSON: {0}/{1}")
    @CsvSource({"BOOK-001,1", "PEN-002,3", "用紙-003,2"})
    void responseEntityKeepsRequestValuesAndWritesJson(String itemCode, int quantity)
            throws Exception {
        mvc.perform(post("/order-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"itemCode\":\"" + itemCode + "\",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemCode").value(itemCode))
                .andExpect(jsonPath("$.quantity").value(quantity))
                .andExpect(jsonPath("$.status").value("preview"));
    }

    @Test
    void httpHeadersSetterAlsoWritesTheExampleAsJson() throws Exception {
        mvc.perform(get("/order-previews/example").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.itemCode").value("BOOK-001"))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.status").value("preview"));
    }
}
