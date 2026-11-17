package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
public class ConditionalController {

    @GetMapping(params = "mode=preview")
    public String preview(@RequestParam(name = "name", defaultValue = "guest") String name) {
        return "preview:" + name;
    }

    @GetMapping(params = "mode=confirm")
    public String confirm(@RequestParam(name = "name", defaultValue = "guest") String name) {
        return "confirm:" + name;
    }

    @GetMapping(path = "/headers", headers = "X-Client=internal")
    public String internal() {
        return "internal";
    }

    @GetMapping(path = "/headers", headers = "X-Client=external")
    public String external() {
        return "external";
    }
}
