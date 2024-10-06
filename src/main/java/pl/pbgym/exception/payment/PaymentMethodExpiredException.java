package pl.pbgym.exception.payment;

public class PaymentMethodExpiredException extends Exception {
    public PaymentMethodExpiredException(String message) {
        super(message);
    }
}
