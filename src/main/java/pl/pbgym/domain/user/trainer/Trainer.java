package pl.pbgym.domain.user.trainer;

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
    @OneToMany(mappedBy = "abstractUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GymEntry> gymEntries;
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

    public List<GymEntry> getGymEntries() {
        return gymEntries;
    }

    public void setGymEntries(List<GymEntry> gymEntries) {
        this.gymEntries = gymEntries;
    }
}
