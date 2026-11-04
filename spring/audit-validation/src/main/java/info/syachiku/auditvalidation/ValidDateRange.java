package info.syachiku.auditvalidation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@interface ValidDateRange {
    String message() default "終了日は開始日以降を指定してください";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
