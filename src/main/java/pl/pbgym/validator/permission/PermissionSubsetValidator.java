package pl.pbgym.validator.permission;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.validator.permission.PermissionSubset;

import java.util.Arrays;
import java.util.List;


public class PermissionSubsetValidator implements ConstraintValidator<PermissionSubset, List<Permissions>> {
    private Permissions[] subset;

    @Override
    public void initialize(PermissionSubset constraint) {
        this.subset = constraint.anyOf();
    }

    @Override
    public boolean isValid(List<Permissions> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return value.stream().allMatch(permission -> Arrays.asList(subset).contains(permission));
    }
}

