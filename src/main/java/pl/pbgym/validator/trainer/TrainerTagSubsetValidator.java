package pl.pbgym.validator.trainer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.pbgym.domain.user.trainer.TrainerTagType;

import java.util.Arrays;
import java.util.List;

public class TrainerTagSubsetValidator implements ConstraintValidator<TrainerTagSubset, List<TrainerTagType>> {
    private TrainerTagType[] subset;

    @Override
    public void initialize(TrainerTagSubset constraint) {
        this.subset = constraint.anyOf();
    }

    @Override
    public boolean isValid(List<TrainerTagType> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return value.stream().allMatch(trainerTag -> Arrays.asList(subset).contains(trainerTag));
    }
}
