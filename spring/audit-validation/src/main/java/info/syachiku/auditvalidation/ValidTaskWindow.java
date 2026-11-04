package info.syachiku.auditvalidation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaskWindowValidator.class)
@interface ValidTaskWindow {
    String message() default "開始日と終了日の順序を確認してください";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
