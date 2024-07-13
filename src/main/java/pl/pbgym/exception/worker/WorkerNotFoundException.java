package pl.pbgym.exception.worker;

public class WorkerNotFoundException extends RuntimeException {
    public WorkerNotFoundException(String message) {
        super(message);
    }
}