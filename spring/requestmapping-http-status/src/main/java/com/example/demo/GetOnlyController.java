package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GetOnlyController {
    @RequestMapping(method = RequestMethod.GET, value = "/sample")
    @ResponseBody
    public String execute1() {
        return "success";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/sample2")
    @ResponseBody
    public String execute2() {
        return "success";
    }
}
