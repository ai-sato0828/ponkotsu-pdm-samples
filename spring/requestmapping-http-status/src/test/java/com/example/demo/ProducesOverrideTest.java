package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

class ProducesOverrideTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProducesOverrideController()).build();
    }

    @Test
    void defaultInheritsClassJsonProduces() throws Exception {
        mvc.perform(get("/reports/default").accept(MediaType.APPLICATION_JSON))
            .andDo(result -> observe("/reports/default", "application/json", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("defaultReport"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"status\":\"ready\"}"));
    }

    @Test
    void inheritedJsonRejectsPlainAccept() throws Exception {
        mvc.perform(get("/reports/default").accept(MediaType.TEXT_PLAIN))
            .andDo(result -> observe("/reports/default", "text/plain", result))
            .andExpect(status().isNotAcceptable())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(HttpMediaTypeNotAcceptableException.class,
                    result.getResolvedException()));
    }

    @Test
    void exportOverridesClassJsonWithPlainProduces() throws Exception {
        mvc.perform(get("/reports/export").accept(MediaType.TEXT_PLAIN))
            .andDo(result -> observe("/reports/export", "text/plain", result))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("export"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string("status=ready"));
    }

    @Test
    void overriddenPlainDoesNotKeepClassJsonAsAlternative() throws Exception {
        mvc.perform(get("/reports/export").accept(MediaType.APPLICATION_JSON))
            .andDo(result -> observe("/reports/export", "application/json", result))
            .andExpect(status().isNotAcceptable())
            .andExpect(result -> assertNull(result.getHandler()))
            .andExpect(result -> assertInstanceOf(HttpMediaTypeNotAcceptableException.class,
                    result.getResolvedException()));
    }

    private static void observe(String path, String accept, MvcResult result) {
        Exception failure = result.getResolvedException();
        System.out.printf("PRODUCES path=%s accept=%s status=%d contentType=%s exception=%s%n",
                path, accept, result.getResponse().getStatus(), result.getResponse().getContentType(),
                failure == null ? "none" : failure.getClass().getSimpleName());
    }
}
