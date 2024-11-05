package pl.pbgym.dto.user.trainer;

import pl.pbgym.domain.user.Gender;
import pl.pbgym.dto.user.GetAddressResponseDto;

import java.time.LocalDate;
import java.util.List;

public class GetTrainerResponseDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private LocalDate birthdate;
    private String pesel;
    private String phoneNumber;
    private GetAddressResponseDto address;
    private String description;
    private byte[] photo;
    private Gender gender;
    private boolean visible;
    private List<String> trainerTags;


    public GetTrainerResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
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

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public GetAddressResponseDto getAddress() {
        return address;
    }

    public void setAddress(GetAddressResponseDto address) {
        this.address = address;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<String> getTrainerTags() {
        return trainerTags;
    }

    public void setTrainerTags(List<String> trainerTags) {
        this.trainerTags = trainerTags;
    }
}
