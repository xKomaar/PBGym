package pl.pbgym.domain.user.trainer;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import pl.pbgym.domain.statistics.GymEntry;
import pl.pbgym.domain.user.AbstractUser;

import java.util.List;

@Entity
@Table(name="trainer")
@PrimaryKeyJoinColumn(name = "trainer_id")
public class Trainer extends AbstractUser {
    @Column(name="description")
    private String description;
    @Column(name="photo")
    private byte[] photo;

    @Column(name="visible", nullable = false)
    private boolean visible;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Nullable
    private List<TrainerTag> trainerTags;

    @OneToMany(mappedBy = "abstractUser", cascade = CascadeType.DETACH, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GymEntry> gymEntries;

    public Trainer() {
        setVisible(false);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
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

    public List<GymEntry> getGymEntries() {
        return gymEntries;
    }

    public void setGymEntries(List<GymEntry> gymEntries) {
        this.gymEntries = gymEntries;
    }
}
