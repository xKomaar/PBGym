package pl.pbgym.exception.user.member;

public class CreditCardInfoAlreadyPresentException extends RuntimeException {
    public CreditCardInfoAlreadyPresentException(String message) {
        super(message);
    }
}
