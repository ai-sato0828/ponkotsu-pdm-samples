package info.syachiku.maintenance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class ViewController {
    @GetMapping("/external-view")
    String external() {
        return "home";
    }

    @GetMapping("/missing-view")
    String missing() {
        return "not-present";
    }

    @GetMapping("/body")
    @ResponseBody
    String body() {
        return "home";
    }
}
