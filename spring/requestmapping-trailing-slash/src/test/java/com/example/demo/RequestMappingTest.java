package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;

class RequestMappingTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SampleController()).build();
    }

    @Test
    void jsonRequestSelectsJsonHandler() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("receiveJson"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"receivedBy\":\"json\"}"));
    }

    @Test
    void formRequestSelectsFormHandler() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML).param("message", "hello"))
            .andExpect(status().isOk())
            .andExpect(handler().methodName("receiveForm"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
            .andExpect(content().string("<result>form</result>"));
    }

    @Test
    void unsupportedMethodIs405WithAllowPost() throws Exception {
        mvc.perform(get("/sample").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(header().string("Allow", "POST"))
            .andExpect(result -> assertInstanceOf(HttpRequestMethodNotSupportedException.class,
                    result.getResolvedException()));
    }

    @Test
    void wrongContentTypeIs415() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_JSON).content("<message>hello</message>"))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(result -> assertInstanceOf(HttpMediaTypeNotSupportedException.class,
                    result.getResolvedException()));
    }

    @Test
    void jsonRequestCannotDemandXmlResponse() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML).content("{\"message\":\"hello\"}"))
            .andExpect(status().isNotAcceptable())
            .andExpect(result -> assertInstanceOf(HttpMediaTypeNotAcceptableException.class,
                    result.getResolvedException()));
    }

    @Test
    void formRequestCannotDemandJsonResponse() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON).param("message", "hello"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    void unknownPathIs404() throws Exception {
        mvc.perform(post("/missing").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content("{\"message\":\"hello\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void missingContentTypeIs415ForThisController() throws Exception {
        mvc.perform(post("/sample").accept(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void malformedJsonIs400AfterMatchingMediaTypes() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content("{invalid}"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertInstanceOf(HttpMessageNotReadableException.class,
                    result.getResolvedException()));
    }

    @Test
    void absentAcceptAllowsJson() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void wildcardAcceptAllowsJson() throws Exception {
        mvc.perform(post("/sample").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL).content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void omittingConsumesAndProducesDoesNotDisableJsonConversion() throws Exception {
        MockMvc automatic = MockMvcBuilders.standaloneSetup(new AutomaticController()).build();
        automatic.perform(post("/automatic").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content("{\"message\":\"hello\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"message\":\"hello\"}"));
    }

    @Test
    void separateGetExampleDoesNotNeedContentType() throws Exception {
        MockMvc getOnly = MockMvcBuilders.standaloneSetup(new GetOnlyController()).build();
        getOnly.perform(get("/sample")).andExpect(status().isOk())
            .andExpect(handler().methodName("execute1"))
            .andExpect(content().string("success"));
        getOnly.perform(get("/sample2")).andExpect(status().isOk())
            .andExpect(handler().methodName("execute2"));
    }

    @Test
    void postToSeparateGetExampleIs405() throws Exception {
        MockMvc getOnly = MockMvcBuilders.standaloneSetup(new GetOnlyController()).build();
        getOnly.perform(post("/sample")).andExpect(status().isMethodNotAllowed());
    }

    @org.springframework.web.bind.annotation.RestController
    static class AutomaticController {
        @org.springframework.web.bind.annotation.PostMapping("/automatic")
        java.util.Map<String, String> echo(
                @org.springframework.web.bind.annotation.RequestBody SampleController.SampleRequest request) {
            return java.util.Map.of("message", request.message());
        }
    }
}
