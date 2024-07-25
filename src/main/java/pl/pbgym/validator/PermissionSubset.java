package pl.pbgym.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.pbgym.domain.user.Permissions;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = PermissionSubsetValidator.class)
public @interface PermissionSubset {
    Permissions[] anyOf();
    String message() default "Permission must be any of {anyOf}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
