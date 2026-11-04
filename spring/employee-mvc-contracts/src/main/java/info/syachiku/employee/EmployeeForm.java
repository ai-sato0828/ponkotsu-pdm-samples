package info.syachiku.employee;

import jakarta.validation.constraints.NotBlank;

public record EmployeeForm(@NotBlank String name) {
}
