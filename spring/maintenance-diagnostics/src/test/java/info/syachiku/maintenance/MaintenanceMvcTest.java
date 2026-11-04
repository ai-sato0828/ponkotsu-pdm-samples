package info.syachiku.maintenance;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.exceptions.TemplateInputException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MaintenanceMvcTest {
    private static final Path templates = temporaryDirectory();

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void templateDirectory(DynamicPropertyRegistry registry) {
        registry.add("demo.templates-directory", templates::toString);
    }

    @AfterAll
    static void removeTemplateDirectory() throws IOException {
        Files.deleteIfExists(templates.resolve("home.html"));
        Files.deleteIfExists(templates);
    }

    @Test
    void ヘッダーの欠落と空白とフォーム項目の検証を別の例外で確認する() throws Exception {
        mvc.perform(get("/userinfo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_HEADER"))
                .andExpect(r -> assertInstanceOf(MissingRequestHeaderException.class, r.getResolvedException()));
        mvc.perform(get("/userinfo").header(HttpHeaders.AUTHORIZATION, " "))
                .andExpect(status().isBadRequest())
                .andExpect(r -> assertInstanceOf(HandlerMethodValidationException.class, r.getResolvedException()));
        mvc.perform(get("/authorize"))
                .andExpect(status().isBadRequest())
                .andExpect(r -> assertInstanceOf(MethodArgumentNotValidException.class, r.getResolvedException()));
        mvc.perform(get("/userinfo").header(HttpHeaders.AUTHORIZATION, "Demo"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/authorize").param("response_type", "code"))
                .andExpect(status().isNoContent());
    }

    @Test
    void paramで異なる三項目を送る() throws Exception {
        mvc.perform(post("/sample").param("userid", "user-01").param("username", "TARO")
                        .param("empno", "1365992"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userid").value("user-01"))
                .andExpect(jsonPath("$.username").value("TARO"))
                .andExpect(jsonPath("$.empno").value("1365992"));
    }

    @Test
    void paramsでは一つの項目名に二つの値を載せられる() throws Exception {
        var request = new LinkedMultiValueMap<String, String>();
        request.add("userid", "user-01");
        request.add("username", "TARO");
        request.add("empno", "1365992");
        request.add("roles", "writer");
        request.add("roles", "reviewer");
        mvc.perform(post("/sample").params(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("writer"))
                .andExpect(jsonPath("$.roles[1]").value("reviewer"));
    }

    @Test
    void 必須項目がないときはBindingResultから400を返す() throws Exception {
        mvc.perform(post("/sample").param("username", "TARO").param("empno", "1365992"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 外部テンプレートを優先し削除すると同梱テンプレートへ戻る() throws Exception {
        Path external = templates.resolve("home.html");
        Files.writeString(external, "<h1>外に置いた画面</h1>");
        try {
            mvc.perform(get("/external-view")).andExpect(status().isOk())
                    .andExpect(content().string(containsString("外に置いた画面")));
        } finally {
            Files.deleteIfExists(external);
        }
        mvc.perform(get("/external-view")).andExpect(status().isOk())
                .andExpect(content().string(containsString("jarに同梱した画面")));
    }

    @Test
    void ViewResolverが同じURLへforwardすると循環エラーになる() {
        var standalone = MockMvcBuilders.standaloneSetup(new CircularController())
                .setViewResolvers(new InternalResourceViewResolver()).build();
        var failure = assertThrows(ServletException.class, () -> standalone.perform(get("/sample")));
        assertTrue(failure.getMessage().contains("Circular view path [sample]"), failure.getMessage());
    }

    @Test
    void Thymeleafのテンプレート欠落と文字列レスポンスは別の経路になる() throws Exception {
        var failure = assertThrows(ServletException.class, () -> mvc.perform(get("/missing-view")));
        assertInstanceOf(TemplateInputException.class, failure.getCause());
        mvc.perform(get("/body")).andExpect(status().isOk()).andExpect(content().string("home"));
    }

    private static Path temporaryDirectory() {
        try {
            return Files.createTempDirectory("spring-external-templates-");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Controller
    static class CircularController {
        @GetMapping("/sample")
        String sample() {
            return "sample";
        }
    }
}
