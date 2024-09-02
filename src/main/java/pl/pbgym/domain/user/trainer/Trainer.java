package pl.pbgym.domain.user.trainer;

import jakarta.persistence.*;
import pl.pbgym.domain.user.AbstractUser;

@Entity
@Table(name="trainer")
@PrimaryKeyJoinColumn(name = "trainer_id")
public class Trainer extends AbstractUser {
    @Column(name="description")
    private String description;
    @Column(name="photo")
    private byte[] photo;

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
}
