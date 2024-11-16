package pl.pbgym.dto.user.trainer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateTrainerOfferRequestDto {
    @NotNull
    private Long id;
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 60, message = "Title can't be longer than 30 characters.")
    @NotEmpty(message = "Title is required.")
    private String title;
    @NotNull(message = "Price is required.")
    @Positive(message = "Price must be positive.")
    private Integer price;
    @NotNull(message = "Training session count is required.")
    @Positive(message = "Training session count must be positive.")
    private Integer trainingSessionCount;
    @NotNull(message = "Training session duration is required.")
    @Positive(message = "Training session duration must be positive.")
    private Integer trainingSessionDurationInMinutes;
    @NotNull(message = "Visibility is required.")
    private boolean isVisible;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getTrainingSessionCount() {
        return trainingSessionCount;
    }

    public void setTrainingSessionCount(Integer trainingSessionCount) {
        this.trainingSessionCount = trainingSessionCount;
    }

    public Integer getTrainingSessionDurationInMinutes() {
        return trainingSessionDurationInMinutes;
    }

    public void setTrainingSessionDurationInMinutes(Integer trainingSessionDurationInMinutes) {
        this.trainingSessionDurationInMinutes = trainingSessionDurationInMinutes;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
