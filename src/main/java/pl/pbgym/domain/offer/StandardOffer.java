package pl.pbgym.domain.offer;

import jakarta.persistence.*;

@Entity
@Table(name="standard_offer")
@PrimaryKeyJoinColumn(name = "standard_offer_id")
public class StandardOffer extends Offer {
    public StandardOffer() {
        this.setType(OfferType.STANDARD);
    }
}
