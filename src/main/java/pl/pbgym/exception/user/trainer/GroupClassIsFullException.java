package pl.pbgym.exception.user.trainer;

public class GroupClassIsFullException extends RuntimeException {
    public GroupClassIsFullException(String message) {
        super(message);
    }
}
