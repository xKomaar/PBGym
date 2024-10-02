package pl.pbgym.exception.payment;

public class PaymentMethodExpiredException extends RuntimeException {
    public PaymentMethodExpiredException(String message) {
        super(message);
    }
}
