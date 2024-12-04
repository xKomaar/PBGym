package pl.pbgym.domain.offer;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="special_offer")
@PrimaryKeyJoinColumn(name = "special_offer_id")
public class SpecialOffer extends Offer {
    @Column(name = "special_offer_text")
    @Nullable
    @Size(min = 3, message = "Special Offer Text can't be shorter than 3 characters.")
    @Size(max = 50, message = "Special Offer Text can't be longer than 50 characters.")
    private String specialOfferText;
    @Column(name = "border_text")
    @Nullable
    @Size(min = 3, message = "Border Text can't be shorter than 3 characters.")
    @Size(max = 30, message = "Border Text can't be longer than 30 characters.")
    private String borderText;
    @Column(name = "previous_price_info")
    @Nullable
    @Size(min = 3, message = "Previous Price Info can't be shorter than 3 characters.")
    @Size(max = 100, message = "Previous Price Info can't be longer than 30 characters.")
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
