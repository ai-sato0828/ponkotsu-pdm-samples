package info.syachiku.employee;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeApiController {
    private final EmployeeDirectory employees;

    public EmployeeApiController(EmployeeDirectory employees) {
        this.employees = employees;
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeResponse detail(@PathVariable long id) {
        return EmployeeResponse.from(employees.find(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeForm form) {
        var employee = employees.create(form.name());
        return ResponseEntity.created(URI.create("/api/employees/" + employee.id()))
                .body(EmployeeResponse.from(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        employees.delete(id);
        return ResponseEntity.noContent().build();
    }
}

record EmployeeResponse(long id, String name, String status) {
    static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.id(), employee.name(), employee.status());
    }
}
