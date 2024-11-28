package pl.pbgym.domain.user.trainer;

import jakarta.persistence.*;

@Entity
@Table(name = "trainer_offer")
public class TrainerOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainer_offer_seq_gen")
    @SequenceGenerator(name="trainer_offer_seq_gen", sequenceName="TRAINER_OFFER_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "training_session_count", nullable = false)
    private Integer trainingSessionCount;

    @Column(name = "training_session_duration_in_minutes", nullable = false)
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
