package pl.pbgym.exception.offer;

public class OfferNotActiveException extends RuntimeException {
    public OfferNotActiveException(String message) {
        super(message);
    }
}
