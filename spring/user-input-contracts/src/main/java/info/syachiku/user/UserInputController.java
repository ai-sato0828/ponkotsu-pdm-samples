package info.syachiku.user;

import info.syachiku.user.UserService.UserView;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
class UserInputController {
    private final UserService userService;

    UserInputController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<UserView> detail(@PathVariable("userId") long userId) {
        return ResponseEntity.of(userService.find(userId));
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<UserView> search(@RequestParam(name = "keyword", defaultValue = "") String keyword,
                                 @RequestParam(name = "page", defaultValue = "1") int page) {
        return userService.search(keyword, page);
    }

    @PostMapping(path = "/api/users", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserView> create(@Valid @RequestBody UserCreateRequest request) {
        UserView user = userService.create(request.name(), request.email(), request.notificationLimit());
        return ResponseEntity.created(URI.create("/api/users/" + user.id())).body(user);
    }

    @GetMapping("/users/new")
    public String newForm(Model model) {
        model.addAttribute("registration", new UserRegistrationForm("", ""));
        return "users/new";
    }

    @PostMapping(path = "/users", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public String register(@Valid @ModelAttribute("registration") UserRegistrationForm form,
                           BindingResult errors) {
        if (errors.hasErrors()) {
            return "users/new";
        }
        userService.create(form.name(), form.email(), 10);
        return "redirect:/users/new";
    }
}
