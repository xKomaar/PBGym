package pl.pbgym.dto.user.trainer;

public class GetTrainerOfferResponseDto {
    private Long id;
    private String title;
    private Integer price;
    private Integer trainingSessionCount;
    private Integer trainingSessionDurationInMinutes;
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
