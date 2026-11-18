package info.syachiku.global;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class GlobalErrorRenderingTest {
    private static final String MESSAGE = "開始日は終了日以前にしてください";
    @Autowired MockMvc mvc;
    @Autowired ApplicationContext context;

    @Test
    void fieldOnlyMarkupMissesGlobalErrorButFixedMarkupShowsIt() throws Exception {
        MvcResult result = mvc.perform(post("/periods")
                .param("startDate", "2026-11-20").param("endDate", "2026-11-19"))
            .andExpect(status().isOk()).andExpect(view().name("periods/input"))
            .andReturn();
        BindingResult errors = errors(result);
        assertTrue(errors.hasErrors());
        assertEquals(0, errors.getFieldErrorCount());
        assertEquals(1, errors.getGlobalErrorCount());
        assertEquals("period.order", errors.getGlobalError().getCode());
        assertEquals(MESSAGE, errors.getGlobalError().getDefaultMessage());
        PeriodForm form = (PeriodForm) result.getModelAndView().getModel().get("periodForm");
        assertEquals(LocalDate.of(2026, 11, 20), form.getStartDate());
        assertEquals(LocalDate.of(2026, 11, 19), form.getEndDate());

        // 同じModelとBindingResultを、表示不足のテスト専用テンプレートへ渡す。
        String before = renderFieldOnly(result);
        String after = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertFalse(before.contains(MESSAGE));
        assertTrue(after.contains(MESSAGE));
        for (String html : new String[] {before, after}) {
            assertTrue(html.contains("value=\"2026-11-20\""));
            assertTrue(html.contains("value=\"2026-11-19\""));
        }
        System.out.println("GLOBAL status=200 hasErrors=true field=0 global=1 beforeText=false afterText=true retained=true");
    }

    @Test
    void missingStartIsFieldErrorAndSkipsCrossFieldComparison() throws Exception {
        MvcResult result = mvc.perform(post("/periods")
                .param("startDate", "").param("endDate", "2026-11-19"))
            .andExpect(status().isOk()).andExpect(view().name("periods/input"))
            .andReturn();
        BindingResult errors = errors(result);
        assertEquals(1, errors.getFieldErrorCount());
        assertEquals(0, errors.getGlobalErrorCount());
        assertEquals("NotNull", errors.getFieldError("startDate").getCode());
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("開始日を入力してください"));
        assertFalse(html.contains(MESSAGE));
        assertTrue(html.contains("value=\"2026-11-19\""));
        System.out.println("FIELD status=200 field=1 global=0 startMessage=true globalText=false");
    }

    @Test
    void validPeriodRedirectsWithoutErrors() throws Exception {
        mvc.perform(post("/periods").param("startDate", "2026-11-19")
                .param("endDate", "2026-11-20"))
            .andExpect(status().isFound()).andExpect(redirectedUrl("/periods/complete"));
        System.out.println("VALID status=302 location=/periods/complete saved=false");
    }

    @Test
    void sameDayIsValid() throws Exception {
        mvc.perform(post("/periods").param("startDate", "2026-11-19")
                .param("endDate", "2026-11-19"))
            .andExpect(status().isFound()).andExpect(redirectedUrl("/periods/complete"));
        System.out.println("SAME_DAY status=302 location=/periods/complete saved=false");
    }

    private BindingResult errors(MvcResult result) {
        return (BindingResult) result.getModelAndView().getModel()
            .get(BindingResult.MODEL_KEY_PREFIX + "periodForm");
    }

    private String renderFieldOnly(MvcResult result) throws Exception {
        String template;
        try (var input = getClass().getResourceAsStream("/templates/periods/field-only.html")) {
            template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setCacheable(false);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setApplicationContext(context);
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        var view = views.resolveViewName(template, java.util.Locale.JAPAN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        view.render(result.getModelAndView().getModel(), result.getRequest(), response);
        return response.getContentAsString(StandardCharsets.UTF_8);
    }
}
