package pl.pbgym.exception.payment;

public class NoPaymentMethodException extends RuntimeException {
    public NoPaymentMethodException(String message) {
        super(message);
    }
}
