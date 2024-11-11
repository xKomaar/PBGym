package pl.pbgym.exception.user.trainer;

public class AlreadyAssignedToThisGroupClassException extends RuntimeException {
    public AlreadyAssignedToThisGroupClassException(String message) {
        super(message);
    }
}
