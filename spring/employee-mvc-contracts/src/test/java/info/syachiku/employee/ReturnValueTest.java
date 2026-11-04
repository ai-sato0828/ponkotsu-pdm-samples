package info.syachiku.employee;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReturnValueTest {
    @Autowired
    MockMvc mvc;

    @Test
    void 同じ社員を画面ではview名とHTMLでAPIではJSONで返す() throws Exception {
        mvc.perform(get("/employees/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/detail"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("田中")));
        mvc.perform(get("/api/employees/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("田中"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.internalNote").doesNotExist());
    }

    @Test
    void RestControllerのStringはView名ではなく本文になる() throws Exception {
        mvc.perform(get("/examples/rest-view-name"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("employees/list"))
                .andExpect(result -> assertNull(result.getModelAndView()));
    }

    @Test
    void MapはControllerではModelになりResponseBodyではJSONになる() throws Exception {
        mvc.perform(get("/examples/page-map"))
                .andExpect(status().isOk())
                .andExpect(view().name("examples/page-map"))
                .andExpect(model().attribute("status", "UP"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
        mvc.perform(get("/examples/page-body"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void API登録は201と新しい社員のLocationを返す() throws Exception {
        var response = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"佐藤\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("佐藤"))
                .andReturn().getResponse();

        Number id = JsonPath.read(response.getContentAsString(), "$.id");
        String location = response.getHeader("Location");
        assertThat(location).isEqualTo("/api/employees/" + id.longValue());
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("佐藤"));
    }
}
