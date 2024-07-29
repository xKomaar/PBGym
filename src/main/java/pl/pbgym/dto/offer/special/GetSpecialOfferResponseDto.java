package pl.pbgym.dto.offer.special;

import pl.pbgym.domain.offer.OfferType;
import pl.pbgym.dto.offer.GetOfferResponseDto;

import java.util.List;

public class GetSpecialOfferResponseDto extends GetOfferResponseDto {
    private String specialOfferText;
    private String borderText;
    private String previousPriceInfo;


    public GetSpecialOfferResponseDto() {
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
