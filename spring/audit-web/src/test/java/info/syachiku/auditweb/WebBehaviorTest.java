package info.syachiku.auditweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebBehaviorTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void 個人データと公開ヘルプのキャッシュ方針が別になる() throws Exception {
        mvc.perform(get("/account/details"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        mvc.perform(get("/help"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=300, public"));
    }

    @Test
    void 引用符とアンパサンドを含む検索値がコンテキストパス付きURLを往復する() throws Exception {
        String query = "O'Reilly & \"book\"";
        String body = mvc.perform(get("/demo/navigation").contextPath("/demo").param("q", query))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var href = Pattern.compile("id=\"details-link\" href=\"([^\"]+)\"").matcher(body);
        var button = Pattern.compile("data-url=\"([^\"]+)\"").matcher(body);
        assertTrue(href.find(), body);
        assertTrue(button.find(), body);
        assertEquals(href.group(1), button.group(1));
        String target = HtmlUtils.htmlUnescape(href.group(1));
        assertTrue(target.startsWith("/demo/details?q="), target);
        mvc.perform(get(URI.create(target)).contextPath("/demo"))
                .andExpect(status().isOk())
                .andExpect(content().string(query));
    }

    @Test
    void HTML属性とJavaScriptの両方に適した形でURLを描画する() throws Exception {
        String body = mvc.perform(get("/demo/assets-example").contextPath("/demo"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains("href=\"/demo/assets/style.css\""), body);
        var literal = Pattern.compile("const assetBaseUrl = ([^;]+);").matcher(body);
        assertTrue(literal.find(), body);
        assertEquals("https://static.example.test/css/",
                new ObjectMapper().readValue(literal.group(1), String.class));
        assertTrue(body.contains("new URL(\"style.css\", assetBaseUrl)"), body);
        mvc.perform(get("/demo/assets/style.css").contextPath("/demo"))
                .andExpect(status().isOk());
    }
}
