package pl.pbgym.dto.offer.special;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import pl.pbgym.dto.offer.PostOfferRequestDto;

public class PostSpecialOfferRequestDto extends PostOfferRequestDto {
    @Nullable
    @Size(min = 3, message = "Special Offer Text can't be shorter than 3 characters.")
    @Size(max = 30, message = "Special Offer Text can't be longer than 30 characters.")
    private String specialOfferText;
    @Nullable
    @Size(min = 3, message = "Border Text can't be shorter than 3 characters.")
    @Size(max = 20, message = "Border Text can't be longer than 30 characters.")
    private String borderText;
    @Nullable
    @Size(min = 3, message = "Previous Price Info can't be shorter than 3 characters.")
    @Size(max = 100, message = "Previous Price Info can't be longer than 30 characters.")
    private String previousPriceInfo;

    public PostSpecialOfferRequestDto() {
    }

    @Nullable
    public String getSpecialOfferText() {
        return specialOfferText;
    }

    public void setSpecialOfferText(@Nullable String specialOfferText) {
        this.specialOfferText = specialOfferText;
    }

    @Nullable
    public String getBorderText() {
        return borderText;
    }

    public void setBorderText(@Nullable String borderText) {
        this.borderText = borderText;
    }

    @Nullable
    public String getPreviousPriceInfo() {
        return previousPriceInfo;
    }

    public void setPreviousPriceInfo(@Nullable String previousPriceInfo) {
        this.previousPriceInfo = previousPriceInfo;
    }
}
