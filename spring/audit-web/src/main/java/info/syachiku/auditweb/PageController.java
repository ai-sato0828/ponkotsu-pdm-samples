package info.syachiku.auditweb;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class PageController {
    @GetMapping("/navigation")
    String navigation(@RequestParam("q") String query, Model model) {
        model.addAttribute("query", query);
        return "navigation";
    }

    @GetMapping("/details")
    @ResponseBody
    String details(@RequestParam("q") String query) {
        return query;
    }

    @GetMapping("/assets-example")
    String assetsExample(Model model) {
        model.addAttribute("assetBaseUrl", "https://static.example.test/css/");
        return "assets-example";
    }
}
