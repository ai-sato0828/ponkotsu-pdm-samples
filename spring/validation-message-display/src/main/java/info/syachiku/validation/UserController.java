package info.syachiku.validation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/users/new")
    public String input(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "users/input";
    }

    @PostMapping("/users")
    public String create(@Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "users/input";
        }
        userService.create(form);
        return "redirect:/users";
    }

    @GetMapping("/users")
    public String list(Model model) {
        model.addAttribute("users", userService.users());
        return "users/list";
    }
}
