package com.example.demo;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/reports", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProducesOverrideController {
    @GetMapping("/default")
    public Map<String, String> defaultReport() {
        return Map.of("status", "ready");
    }

    @GetMapping(value = "/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public String export() {
        return "status=ready";
    }
}
