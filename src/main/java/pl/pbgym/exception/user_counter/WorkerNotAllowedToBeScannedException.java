package pl.pbgym.exception.user_counter;

public class WorkerNotAllowedToBeScannedException extends RuntimeException {
    public WorkerNotAllowedToBeScannedException(String message) {
        super(message);
    }
}
