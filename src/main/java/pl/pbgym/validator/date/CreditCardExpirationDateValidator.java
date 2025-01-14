package pl.pbgym.validator.date;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.pbgym.dto.user.member.PostCreditCardInfoRequestDto;

import java.time.LocalDate;
import java.time.YearMonth;

public class CreditCardExpirationDateValidator implements ConstraintValidator<ValidCreditCardExpirationDate, PostCreditCardInfoRequestDto> {

    @Override
    public boolean isValid(PostCreditCardInfoRequestDto dto, ConstraintValidatorContext context) {
        try {
            int month = Integer.parseInt(dto.getExpirationMonth());
            int year = Integer.parseInt("20" + dto.getExpirationYear());

            YearMonth expirationDate = YearMonth.of(year, month);
            YearMonth currentYearMonth = YearMonth.from(LocalDate.now());

            // The card is valid if the expiration date is in the future
            return expirationDate.isAfter(currentYearMonth) || expirationDate.equals(currentYearMonth);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
