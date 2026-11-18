package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.NoHandlerFoundException;

class TrailingSlashMappingTest {
    @Test
    void singleMappingAcceptsExactPath() throws Exception {
        MockMvcBuilders.standaloneSetup(new GetOnlyController()).build()
            .perform(get("/sample"))
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(GetOnlyController.class))
            .andExpect(handler().methodName("execute1"))
            .andExpect(content().string("success"))
            .andExpect(result -> assertNull(result.getResolvedException()))
            .andDo(result -> observe("single", result));
    }

    @Test
    void singleMappingDoesNotAcceptTrailingSlash() throws Exception {
        MockMvcBuilders.standaloneSetup(new GetOnlyController()).build()
            .perform(get("/sample/"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(NoHandlerFoundException.class,
                    result.getResolvedException()))
            .andDo(result -> observe("single", result));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/sample", "/sample/"})
    void explicitMappingsAcceptBothPaths(String path) throws Exception {
        MockMvcBuilders.standaloneSetup(new TrailingSlashController()).build()
            .perform(get(path))
            .andExpect(status().isOk())
            .andExpect(handler().handlerType(TrailingSlashController.class))
            .andExpect(handler().methodName("execute"))
            .andExpect(content().string("success"))
            .andExpect(result -> assertNull(result.getResolvedException()))
            .andDo(result -> observe("explicit", result));
    }

    private static void observe(String mapping, MvcResult result) throws Exception {
        Object handler = result.getHandler();
        String selected = handler instanceof HandlerMethod method
                ? method.getBeanType().getSimpleName() + "." + method.getMethod().getName()
                : "none";
        Exception failure = result.getResolvedException();
        System.out.printf("SLASH mapping=%s method=%s path=%s status=%d handler=%s exception=%s body=%s%n",
                mapping, result.getRequest().getMethod(), result.getRequest().getRequestURI(),
                result.getResponse().getStatus(), selected,
                failure == null ? "none" : failure.getClass().getSimpleName(),
                result.getResponse().getContentAsString());
    }
}
