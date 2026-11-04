package info.syachiku.auditvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskWindowValidator implements ConstraintValidator<ValidTaskWindow, TaskInput> {
    @Override
    public boolean isValid(TaskInput input, ConstraintValidatorContext context) {
        return input == null || input.startDate() == null || input.endDate() == null
                || !input.endDate().isBefore(input.startDate());
    }
}
