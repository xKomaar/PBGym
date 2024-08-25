package pl.pbgym.domain.offer;

import jakarta.persistence.*;

@Entity
@Table(name="special_offer")
@PrimaryKeyJoinColumn(name = "special_offer_id")
public class SpecialOffer extends Offer {
    @Column(name = "specialOfferText", nullable = true)
    private String specialOfferText;
    @Column(name = "borderText", nullable = true)
    private String borderText;
    @Column(name = "previousPriceInfo", nullable = true)
    private String previousPriceInfo;

    public SpecialOffer() {
        this.setType(OfferType.SPECIAL);
    }

    public String getSpecialOfferText() {
        return specialOfferText;
    }

    public void setSpecialOfferText(String specialOfferText) {
        this.specialOfferText = specialOfferText;
    }

    public String getBorderText() {
        return borderText;
    }

    public void setBorderText(String borderText) {
        this.borderText = borderText;
    }

    public String getPreviousPriceInfo() {
        return previousPriceInfo;
    }

    public void setPreviousPriceInfo(String previousPriceInfo) {
        this.previousPriceInfo = previousPriceInfo;
    }
}
