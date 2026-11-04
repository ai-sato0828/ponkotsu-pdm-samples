package info.syachiku.employee;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
class PageReturnExamples {
    @GetMapping("/examples/page-map")
    Map<String, String> asModel() {
        return Map.of("status", "UP");
    }

    @ResponseBody
    @GetMapping("/examples/page-body")
    Map<String, String> asBody() {
        return Map.of("status", "UP");
    }
}

@RestController
class RestReturnExamples {
    @GetMapping(value = "/examples/rest-view-name", produces = MediaType.TEXT_PLAIN_VALUE)
    String asBody() {
        return "employees/list";
    }
}
