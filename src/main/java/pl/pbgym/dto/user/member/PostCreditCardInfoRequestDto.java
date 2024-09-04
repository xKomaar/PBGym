package pl.pbgym.dto.user.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pl.pbgym.validator.date.ValidCreditCardExpirationDate;

@ValidCreditCardExpirationDate
public class PostCreditCardInfoRequestDto {
    @NotBlank(message = "Card number month is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
    private String cardNumber;
    @NotBlank(message = "Expiration month is required")
    @Pattern(regexp = "0[1-9]|1[0-2]", message = "Expiration month must be between 01 and 12")
    private String expirationMonth;
    @NotBlank(message = "Expiration year is required")
    @Pattern(regexp = "\\d{2}", message = "Expiration year must be 2 digits")
    private String expirationYear;
    @Pattern(regexp = "\\d{3}", message = "CVC must consist of 3 digits")
    private String cvc;


    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(String expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public String getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(String expirationYear) {
        this.expirationYear = expirationYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
}
