package pl.pbgym.exception.user.member;

public class CreditCardInfoNotFoundException extends RuntimeException {
    public CreditCardInfoNotFoundException(String message) {
        super(message);
    }
}
