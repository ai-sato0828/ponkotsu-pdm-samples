package info.syachiku.audit;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class RequestPageController {
    private final RequestService service;

    RequestPageController(RequestService service) {
        this.service = service;
    }

    @PostMapping("/requests")
    String create(@ModelAttribute RequestForm form, RedirectAttributes redirect) {
        long id = service.create(form);
        redirect.addFlashAttribute("notice", "申請を登録しました");
        return "redirect:/requests/" + id;
    }

    @PostMapping("/requests/once")
    String createOnce(@ModelAttribute RequestForm form, RedirectAttributes redirect) {
        long id = service.createOnce(form);
        redirect.addFlashAttribute("notice", "申請を登録しました");
        return "redirect:/requests/" + id;
    }

    @GetMapping("/requests/{id}")
    String detail(@PathVariable long id, Model model) {
        model.addAttribute("request", service.find(id));
        return "requests/detail";
    }
}
