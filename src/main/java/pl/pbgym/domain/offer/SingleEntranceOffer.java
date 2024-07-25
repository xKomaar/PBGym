package pl.pbgym.domain.offer;

import jakarta.persistence.*;

@Entity
@Table(name="single_entrance_offer")
@PrimaryKeyJoinColumn(name = "single_entrance_offer_id")
public class SingleEntranceOffer extends Offer {
    @Basic
    @Column(name = "entryFee", nullable = false)
    private Integer numberOfEntries;

    public SingleEntranceOffer() {
    }

    public Integer getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(Integer numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }
}
