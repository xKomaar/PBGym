package pl.pbgym.domain.offer;

import jakarta.persistence.*;

@Entity
@Table(name="standard_offer")
@PrimaryKeyJoinColumn(name = "standard_offer_id")
public class StandardOffer extends Offer {
    @Basic
    @Column(name = "entryFee", nullable = false)
    private Double entryFee;

    public Double getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(Double entryFee) {
        this.entryFee = entryFee;
    }
}
