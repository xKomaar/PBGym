package pl.pbgym.validator.list;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ListSizeValidator implements ConstraintValidator<ListSize, List<?>> {

    private int maxCount;
    @Override
    public void initialize(ListSize constraintAnnotation) {
        this.maxCount = constraintAnnotation.maxCount();
    }

    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.size() <= maxCount;
    }
}
