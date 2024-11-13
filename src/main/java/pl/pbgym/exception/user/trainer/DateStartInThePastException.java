package pl.pbgym.exception.user.trainer;

public class DateStartInThePastException extends RuntimeException {
    public DateStartInThePastException(String message) {
        super(message);
    }
}
