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

import org.springframework.web.method.annotation.HandlerMethodValidationException;

@SpringBootTest(classes = MvcValidationTest.Context.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class MvcValidationTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import({ItemsController.class, InputValidationAdvice.class})
    static class Context {}

    @Autowired MockMvc mvc;
    @Autowired ItemsController controller;

    @Test
    void blankKeywordUsesMvcExceptionHandledByTheNarrowAdvice() throws Exception {
        assertFalse(AopUtils.isAopProxy(controller));
        var result = mvc.perform(get("/items").param("keyword", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"code\":\"INVALID_PARAMETER\"}"))
                .andReturn();
        var failure = assertInstanceOf(HandlerMethodValidationException.class,
                result.getResolvedException());
        assertFalse(failure.isForReturnValue());
        assertEquals(1, failure.getAllValidationResults().size());
        assertEquals("   ", failure.getAllValidationResults().get(0).getArgument());
        System.out.println("MVC keyword=spaces status=400 exception=HandlerMethodValidationException code=INVALID_PARAMETER");
    }

    @Test
    void validKeywordReachesTheController() throws Exception {
        mvc.perform(get("/items").param("keyword", "java"))
                .andExpect(status().isOk()).andExpect(content().json("{\"keyword\":\"java\"}"));
        System.out.println("MVC keyword=java status=200 bodyKeyword=java");
    }
}
