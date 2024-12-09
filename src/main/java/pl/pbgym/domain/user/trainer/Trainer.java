package pl.pbgym.domain.user.trainer;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.domain.user.AbstractUser;
import pl.pbgym.validator.list.ListSize;
import pl.pbgym.validator.trainer.TrainerTagSubset;

import java.util.List;

@Entity
@Table(name="trainer")
@PrimaryKeyJoinColumn(name = "trainer_id")
public class Trainer extends AbstractUser {
    @Column(name="description")
    @Size(min = 2, message = "Description can't be shorter than 2 characters.")
    @Size(max = 1000, message = "Description can't be longer than 1000 characters.")
    private String description;
    @Column(name = "photo", columnDefinition = "TEXT")
    private String photo;
    @Column(name="visible", nullable = false)
    private boolean visible;
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TrainerTag> trainerTags;
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TrainerOffer> trainerOffers;
    @OneToMany(mappedBy = "abstractUser", cascade = CascadeType.DETACH, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GymEntry> gymEntries;
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupClass> groupClasses;

    public Trainer() {
        setVisible(false);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<TrainerTag> getTrainerTags() {
        return trainerTags;
    }

    public void setTrainerTags(List<TrainerTag> trainerTags) {
        this.trainerTags = trainerTags;
    }

    public List<TrainerOffer> getTrainerOffers() {
        return trainerOffers;
    }

    public void setTrainerOffers(List<TrainerOffer> trainerOffers) {
        this.trainerOffers = trainerOffers;
    }

    public List<GymEntry> getGymEntries() {
        return gymEntries;
    }

    public void setGymEntries(List<GymEntry> gymEntries) {
        this.gymEntries = gymEntries;
    }

    public List<GroupClass> getGroupClasses() {
        return groupClasses;
    }

    public void setGroupClasses(List<GroupClass> groupClasses) {
        this.groupClasses = groupClasses;
    }
}
