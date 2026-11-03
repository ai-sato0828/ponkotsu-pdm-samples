package info.syachiku.validation;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ValidationApplication.class)
@AutoConfigureMockMvc
@Import(UserFormHtmlTest.RedirectOnErrorController.class)
@Execution(ExecutionMode.SAME_THREAD)
class UserFormHtmlTest {
    private static final String EMAIL = "test@example.com";
    private static final String REQUIRED = "名前を入力してください。";
    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired SpringTemplateEngine templateEngine;
    @Autowired @Qualifier("defaultTemplateResolver")
    SpringResourceTemplateResolver templateResolver;

    @BeforeEach
    void resetInMemoryService() { userService.reset(); }

    @Test
    void initialGetDisplaysFormWithoutErrors() throws Exception {
        mockMvc.perform(get("/users/new").locale(Locale.JAPANESE))
                .andExpect(status().isOk())
                .andExpect(view().name("users/input"))
                .andExpect(model().attributeExists("userForm"))
                .andExpect(content().string(containsString("name=\"name\" value=\"\"")))
                .andExpect(content().string(not(containsString("id=\"name-error\""))));
        assertNoRegistration();
    }

    @Test
    void blankNameShowsJapaneseMessageAndKeepsEmailWithoutSaving() throws Exception {
        MvcResult result = submitInvalidName("");
        assertThat(html(result)).contains("<p id=\"name-error\">" + REQUIRED + "</p>")
                .contains("name=\"email\" value=\"" + EMAIL + "\"");
        assertNoRegistration();
    }

    @Test
    void whitespaceOnlyNameAlsoFailsNotBlank() throws Exception {
        MvcResult result = submitInvalidName("   ");
        assertThat(html(result)).contains(REQUIRED);
        assertNoRegistration();
    }

    @Test
    void tooLongNameShowsLengthMessageInsteadOfRequiredMessage() throws Exception {
        MvcResult result = submitInvalidName("a".repeat(51));
        assertThat(html(result)).contains("名前は50文字以内で入力してください。")
                .doesNotContain(REQUIRED);
        assertNoRegistration();
    }

    @Test
    void invalidEmailShowsItsOwnErrorAndKeepsName() throws Exception {
        mockMvc.perform(post("/users").locale(Locale.JAPANESE)
                        .param("name", "山田太郎").param("email", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/input"))
                .andExpect(model().attributeHasFieldErrorCode("userForm", "email", "Email"))
                .andExpect(model().errorCount(1))
                .andExpect(content().string(containsString(
                        "<p id=\"email-error\">メールアドレスの形式で入力してください。</p>")))
                .andExpect(content().string(containsString("name=\"name\" value=\"山田太郎\"")));
        assertNoRegistration();
    }

    @Test
    void validPostCallsServiceOnceAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/users").locale(Locale.JAPANESE)
                        .param("name", "山田太郎").param("email", EMAIL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users"));
        assertThat(userService.createCalls()).isEqualTo(1);
        assertThat(userService.users())
                .containsExactly(new UserService.RegisteredUser("山田太郎", EMAIL));
        mockMvc.perform(get("/users").locale(Locale.JAPANESE))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<li>山田太郎</li>")));
        assertThat(userService.createCalls()).isEqualTo(1);
    }

    @Test
    void brokenAndFixedTemplatesHaveSameFieldErrorButDifferentHtml() throws Exception {
        String broken = resource("comparison/broken/users/input.html");
        String fixed = resource("comparison/fixed/users/input.html");
        assertThat(fixed).isEqualTo(resource("templates/users/input.html"));
        assertThat(broken).isEqualTo(fixed.replace("th:errors=\"*{name}\"", "th:errors=\"*{email}\""));

        String originalPrefix = templateResolver.getPrefix();
        try {
            templateResolver.setPrefix("classpath:/comparison/broken/");
            templateEngine.clearTemplateCache();
            MvcResult before = submitInvalidName("");

            templateResolver.setPrefix("classpath:/comparison/fixed/");
            templateEngine.clearTemplateCache();
            MvcResult after = submitInvalidName("");

            assertThat(html(before)).doesNotContain("id=\"name-error\"").doesNotContain(REQUIRED);
            assertThat(html(after)).contains("<p id=\"name-error\">" + REQUIRED + "</p>");
            assertThat(html(before)).contains("name=\"email\" value=\"" + EMAIL + "\"");
            assertThat(html(after)).contains("name=\"email\" value=\"" + EMAIL + "\"");
            assertNoRegistration();
        } finally {
            templateResolver.setPrefix(originalPrefix);
            templateEngine.clearTemplateCache();
        }
    }

    @Test
    void redirectWithoutFlashLosesFieldErrorAndInputOnNextGet() throws Exception {
        mockMvc.perform(post("/comparison/redirect/users").locale(Locale.JAPANESE)
                        .param("name", "").param("email", EMAIL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/new"));
        mockMvc.perform(get("/users/new").locale(Locale.JAPANESE))
                .andExpect(status().isOk())
                .andExpect(view().name("users/input"))
                .andExpect(content().string(not(containsString(REQUIRED))))
                .andExpect(content().string(containsString("name=\"email\" value=\"\"")));
        assertNoRegistration();
    }

    private MvcResult submitInvalidName(String name) throws Exception {
        return mockMvc.perform(post("/users").locale(Locale.JAPANESE)
                        .param("name", name).param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("users/input"))
                .andExpect(model().attributeHasFieldErrors("userForm", "name"))
                .andExpect(model().errorCount(1))
                .andReturn();
    }

    private void assertNoRegistration() {
        assertThat(userService.createCalls()).isZero();
        assertThat(userService.users()).isEmpty();
    }

    private static String resource(String path) throws Exception {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    private static String html(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @TestComponent
    @Controller
    static class RedirectOnErrorController {
        @PostMapping("/comparison/redirect/users")
        String redirect(@Valid @ModelAttribute("userForm") UserForm form,
                        BindingResult result) {
            assertThat(result.getFieldErrorCount("name")).isEqualTo(1);
            return "redirect:/users/new";
        }
    }
}
