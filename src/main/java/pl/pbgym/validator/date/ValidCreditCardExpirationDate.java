package pl.pbgym.validator.date;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = CreditCardExpirationDateValidator.class)
@Target({FIELD, TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface ValidCreditCardExpirationDate {

    String message() default "Invalid expiration date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
