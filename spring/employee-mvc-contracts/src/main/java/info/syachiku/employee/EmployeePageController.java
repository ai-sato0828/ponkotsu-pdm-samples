package info.syachiku.employee;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeePageController {
    private final EmployeeDirectory employees;

    public EmployeePageController(EmployeeDirectory employees) {
        this.employees = employees;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", employees.findAll());
        model.addAttribute("employeeForm", new EmployeeForm(""));
        return "employees/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("employee", employees.find(id));
        return "employees/detail";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("employeeForm") EmployeeForm form,
                         BindingResult errors) {
        if (errors.hasErrors()) {
            return "employees/form";
        }
        return "redirect:/employees/" + employees.create(form.name()).id();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id) {
        employees.delete(id);
        return "redirect:/employees";
    }
}
