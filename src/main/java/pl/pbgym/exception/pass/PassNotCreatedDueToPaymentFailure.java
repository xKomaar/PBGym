package pl.pbgym.exception.pass;

public class PassNotCreatedDueToPaymentFailure extends RuntimeException {
    public PassNotCreatedDueToPaymentFailure(String message) {
        super(message);
    }
}
