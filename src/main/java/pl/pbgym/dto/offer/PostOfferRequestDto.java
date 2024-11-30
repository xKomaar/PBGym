package pl.pbgym.dto.offer;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pl.pbgym.validator.list.ListSize;

import java.util.List;

public abstract class PostOfferRequestDto {
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 60, message = "Title can't be longer than 60 characters.")
    @NotEmpty(message = "Title is required.")
    private String title;
    @Size(min = 5, message = "Subtitle can't be shorter than 5 characters.")
    @Size(max = 50, message = "Subtitle can't be longer than 50 characters.")
    @NotEmpty(message = "Subtitle is required.")
    private String subtitle;
    @NotNull(message = "Monthly is required.")
    @Positive(message = "Price must be positive.")
    private Double monthlyPrice;
    @NotNull(message = "Entry Fee is required.")
    @Positive(message = "Price must be positive.")
    private Double entryFee;
    @NotNull(message = "isActive is required.")
    private boolean isActive;
    @NotNull(message = "Duration in months is required.")
    private Integer durationInMonths;
    @Nullable
    @ListSize(maxCount = 6)
    private List<String> properties;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    @Nullable
    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(@Nullable List<String> properties) {
        this.properties = properties;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(Double monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Double getEntryFee() {
        return entryFee;
    }

    public void setEntryFee(Double entryFee) {
        this.entryFee = entryFee;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Integer getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(Integer durationInMonths) {
        this.durationInMonths = durationInMonths;
    }
}
