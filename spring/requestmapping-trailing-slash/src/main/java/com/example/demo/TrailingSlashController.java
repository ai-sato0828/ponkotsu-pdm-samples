package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrailingSlashController {
    @GetMapping({"/sample", "/sample/"})
    public String execute() {
        return "success";
    }
}
