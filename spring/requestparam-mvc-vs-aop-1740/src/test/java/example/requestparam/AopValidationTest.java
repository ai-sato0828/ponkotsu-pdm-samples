package example.requestparam;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;

@SpringBootTest(classes = AopValidationTest.Context.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AopValidationTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import({ValidatedItemsController.class, InputValidationAdvice.class})
    static class Context {}

    @Autowired MockMvc mvc;
    @Autowired ValidatedItemsController controller;

    @Test
    void blankKeywordUsesAopExceptionNotHandledByTheMvcOnlyAdvice() {
        assertTrue(AopUtils.isAopProxy(controller));
        var wrapped = assertThrows(ServletException.class,
                () -> mvc.perform(get("/items").param("keyword", "   ")));
        Throwable root = wrapped;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        var failure = assertInstanceOf(ConstraintViolationException.class, root);
        assertEquals(1, failure.getConstraintViolations().size());
        var violation = failure.getConstraintViolations().iterator().next();
        assertEquals("   ", violation.getInvalidValue());
        assertEquals(NotBlank.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        System.out.println("AOP keyword=spaces outcome=ServletException root=ConstraintViolationException httpStatus=not-observed");
    }

    @Test
    void validKeywordAlsoReachesTheControllerThroughTheAopProxy() throws Exception {
        mvc.perform(get("/items").param("keyword", "java"))
                .andExpect(status().isOk()).andExpect(content().json("{\"keyword\":\"java\"}"));
        System.out.println("AOP keyword=java status=200 bodyKeyword=java");
    }
}
