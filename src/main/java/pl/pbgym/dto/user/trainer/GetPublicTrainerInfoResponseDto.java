package pl.pbgym.dto.user.trainer;

import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.trainer.TrainerTagType;

import java.util.List;

public class GetPublicTrainerInfoResponseDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phoneNumber;
    private String description;
    private byte[] photo;
    private Gender gender;
    private List<TrainerTagType> trainerTags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<TrainerTagType> getTrainerTags() {
        return trainerTags;
    }

    public void setTrainerTags(List<TrainerTagType> trainerTags) {
        this.trainerTags = trainerTags;
    }
}
