package com.example.demo;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @RequestMapping(method = RequestMethod.POST, value = "/sample",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> receiveJson(@RequestBody SampleRequest request) {
        return Map.of("receivedBy", "json");
    }

    @RequestMapping(method = RequestMethod.POST, value = "/sample",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public String receiveForm(@RequestParam("message") String message) {
        return "<result>form</result>";
    }

    public record SampleRequest(String message) { }
}
