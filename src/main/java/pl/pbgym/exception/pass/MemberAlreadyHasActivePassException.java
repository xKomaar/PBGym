package pl.pbgym.exception.pass;

public class MemberAlreadyHasActivePassException extends RuntimeException {
    public MemberAlreadyHasActivePassException(String message) {
        super(message);
    }
}
