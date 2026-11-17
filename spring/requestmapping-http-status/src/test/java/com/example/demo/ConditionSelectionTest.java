package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;

class ConditionSelectionTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ConditionalController()).build();
    }

    @Test
    void previewParameterSelectsPreview() throws Exception {
        mvc.perform(get("/sample").param("mode", "preview").param("name", "Alice"))
            .andDo(result -> observe("mode=preview&name=Alice", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("preview"))
            .andExpect(content().string("preview:Alice"));
    }

    @Test
    void confirmParameterSelectsConfirm() throws Exception {
        mvc.perform(get("/sample").param("mode", "confirm").param("name", "Bob"))
            .andDo(result -> observe("mode=confirm&name=Bob", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("confirm"))
            .andExpect(content().string("confirm:Bob"));
    }

    @Test
    void requestParamDefaultDoesNotReplaceModeCondition() throws Exception {
        mvc.perform(get("/sample").param("mode", "preview"))
            .andDo(result -> observe("mode=preview (name omitted)", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("preview"))
            .andExpect(content().string("preview:guest"));
    }

    @Test
    void missingModeIs400BeforeCallingController() throws Exception {
        mvc.perform(get("/sample").param("name", "Alice"))
            .andDo(result -> observe("mode omitted", result))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(UnsatisfiedServletRequestParameterException.class,
                    result.getResolvedException()));
    }

    @Test
    void unknownModeIs400BeforeCallingController() throws Exception {
        mvc.perform(get("/sample").param("mode", "unknown"))
            .andDo(result -> observe("mode=unknown", result))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(UnsatisfiedServletRequestParameterException.class,
                    result.getResolvedException()));
    }

    @Test
    void internalHeaderSelectsInternal() throws Exception {
        mvc.perform(get("/sample/headers").header("X-Client", "internal"))
            .andDo(result -> observe("X-Client=internal", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("internal"))
            .andExpect(content().string("internal"));
    }

    @Test
    void externalHeaderSelectsExternal() throws Exception {
        mvc.perform(get("/sample/headers").header("X-Client", "external"))
            .andDo(result -> observe("X-Client=external", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("external"))
            .andExpect(content().string("external"));
    }

    @Test
    void missingHeaderIs404ForThisController() throws Exception {
        mvc.perform(get("/sample/headers"))
            .andDo(result -> observe("X-Client omitted", result))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(NoHandlerFoundException.class,
                    result.getResolvedException()));
    }

    @Test
    void unknownHeaderIs404ForThisController() throws Exception {
        mvc.perform(get("/sample/headers").header("X-Client", "partner"))
            .andDo(result -> observe("X-Client=partner", result))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(NoHandlerFoundException.class,
                    result.getResolvedException()));
    }

    private static void observe(String input, MvcResult result) throws Exception {
        String selected = result.getHandler() instanceof HandlerMethod method
                ? method.getMethod().getName() : "none";
        String exception = result.getResolvedException() == null
                ? "none" : result.getResolvedException().getClass().getSimpleName();
        System.out.printf("INPUT=%s STATUS=%d HANDLER=%s EXCEPTION=%s BODY=%s%n",
                input, result.getResponse().getStatus(), selected, exception,
                result.getResponse().getContentAsString());
    }
}
