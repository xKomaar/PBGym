package pl.pbgym.validator.list;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ListSizeValidator.class)
public @interface ListSize {
    int maxCount();
    String message() default "The list must contain no more than {maxCount} entries";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
