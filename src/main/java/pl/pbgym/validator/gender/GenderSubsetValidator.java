package pl.pbgym.validator.gender;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.pbgym.domain.user.Gender;

import java.util.Arrays;
import java.util.List;

public class GenderSubsetValidator implements ConstraintValidator<GenderSubset, Gender> {
    private Gender[] subset;

    @Override
    public void initialize(GenderSubset constraint) {
        this.subset = constraint.anyOf();
    }

    @Override
    public boolean isValid(Gender value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Arrays.asList(subset).contains(value);
    }
}
