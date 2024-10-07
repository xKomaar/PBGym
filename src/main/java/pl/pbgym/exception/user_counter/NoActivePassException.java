package pl.pbgym.exception.user_counter;

public class NoActivePassException extends RuntimeException {
    public NoActivePassException(String message) {
        super(message);
    }
}
