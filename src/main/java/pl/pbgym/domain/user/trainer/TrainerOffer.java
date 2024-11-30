package pl.pbgym.domain.user.trainer;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "trainer_offer")
public class TrainerOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainer_offer_seq_gen")
    @SequenceGenerator(name="trainer_offer_seq_gen", sequenceName="TRAINER_OFFER_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    @Size(min = 3, message = "Title can't be shorter than 3 characters.")
    @Size(max = 60, message = "Title can't be longer than 60 characters.")
    private String title;
    @Column(name = "price", nullable = false)
    @Positive(message = "Price must be positive.")
    private Integer price;
    @Column(name = "training_session_count", nullable = false)
    @Positive(message = "Training session count must be positive.")
    private Integer trainingSessionCount;
    @Column(name = "training_session_duration_in_minutes", nullable = false)
    @Positive(message = "Training session duration must be positive.")
    private Integer trainingSessionDurationInMinutes;
    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;
    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

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

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }
}
