package pl.pbgym.exception.user.trainer;

public class TrainerDoesntOwnOfferException extends RuntimeException {
    public TrainerDoesntOwnOfferException(String message) {
        super(message);
    }
}
