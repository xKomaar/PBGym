package pl.pbgym.domain;

import jakarta.persistence.*;

@Entity
@Table(name="trainer")
@PrimaryKeyJoinColumn(name = "user_id")
public class Trainer extends AbstractUser {

    @Basic
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
