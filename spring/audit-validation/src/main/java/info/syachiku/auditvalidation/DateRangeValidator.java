package info.syachiku.auditvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, RequestDates> {
    @Override
    public boolean isValid(RequestDates input, ConstraintValidatorContext context) {
        if (input == null || input.startDate() == null || input.endDate() == null) {
            return true;
        }
        if (!input.endDate().isBefore(input.startDate())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("endDate").addConstraintViolation();
        return false;
    }
}
