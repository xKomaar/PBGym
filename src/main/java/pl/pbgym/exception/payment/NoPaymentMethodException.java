package pl.pbgym.exception.payment;

public class NoPaymentMethodException extends Exception {
    public NoPaymentMethodException(String message) {
        super(message);
    }
}
