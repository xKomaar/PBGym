package pl.pbgym.dto.offer;

public class OfferNotActiveException extends RuntimeException {
    public OfferNotActiveException(String message) {
        super(message);
    }
}
