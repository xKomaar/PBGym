package pl.pbgym.domain.user.trainer;

import jakarta.persistence.*;

@Entity
@Table(name="trainer_tag")
public class TrainerTag {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trainer_tag_gen")
    @SequenceGenerator(name = "trainer_tag_gen", sequenceName = "TRAINER_TAG_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name="tag", nullable = false)
    @Enumerated(EnumType.STRING)
    private TrainerTagType tag;
    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainerTagType getTag() {
        return tag;
    }

    public void setTag(TrainerTagType tag) {
        this.tag = tag;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }
}
