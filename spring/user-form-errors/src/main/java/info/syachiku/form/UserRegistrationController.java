package info.syachiku.form;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class UserRegistrationController {
    private static final List<Department> DEPARTMENTS = List.of(
            new Department("DEV", "開発"),
            new Department("OPS", "運用"));
    private final UserService userService;

    UserRegistrationController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("departments")
    public List<Department> departments() {
        return DEPARTMENTS;
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("userRegisterForm", new UserRegisterForm("", "DEV", 1));
        return "users/new";
    }

    @GetMapping("/demo/users/wrong-form-name")
    public String wrongFormName(Model model) {
        model.addAttribute("form", new UserRegisterForm("", "DEV", 1));
        return "users/new";
    }

    @PostMapping("/users")
    public String register(@Valid @ModelAttribute("userRegisterForm") UserRegisterForm form,
                           BindingResult errors, Model model) {
        if (form.getDepartment() != null && !form.getDepartment().isBlank()
                && DEPARTMENTS.stream().noneMatch(department -> department.code().equals(form.getDepartment()))) {
            errors.rejectValue("department", "unknownDepartment", "一覧から部署を選択してください");
        }
        if (errors.hasErrors()) {
            model.addAttribute("showCorrectionNotice", true);
            return "users/new";
        }
        userService.register(form.getName(), form.getDepartment(), form.getSeatLimit());
        return "redirect:/users/new?registered";
    }

    @PostMapping("/demo/users/wrong-order")
    public String wrongOrder(@Valid @ModelAttribute("userRegisterForm") UserRegisterForm form,
                             Model model, BindingResult errors) {
        return register(form, errors, model);
    }

    @PostMapping("/demo/users/implicit")
    public String implicit(@Valid UserRegisterForm form, BindingResult errors, Model model) {
        return register(form, errors, model);
    }

    public record Department(String code, String label) {}
}
