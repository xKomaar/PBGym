package pl.pbgym.dto.user.trainer;

import jakarta.annotation.Nullable;
import jakarta.persistence.Basic;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.domain.user.trainer.TrainerTagType;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.validator.gender.GenderSubset;
import pl.pbgym.validator.list.ListSize;
import pl.pbgym.validator.trainer.TrainerTagSubset;

import java.time.LocalDate;
import java.util.List;

public class UpdateTrainerRequestDto {
    @Size(min = 2, message = "Name can't be shorter than 2 characters.")
    @Size(max = 50, message = "Name can't be longer than 50 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Name has to begin with a capital letter and involve only letters.")
    @NotBlank(message = "Name is required.")
    private String name;
    @Size(min = 2, message = "Surname can't be shorter than 2 characters.")
    @Size(max = 100, message = "Surname can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Surname has to begin with a capital letter and involve only letters.")
    @NotBlank(message = "Surname is required.")
    private String surname;
    @NotNull(message = "Birthdate is required.")
    private LocalDate birthdate;
    @NotNull(message = "Gender is required.")
    @GenderSubset(anyOf = {
            Gender.FEMALE,
            Gender.MALE,
            Gender.OTHER
    }, message = "Gender need to be MALE, FEMALE or OTHER")
    private Gender gender;
    @NotBlank(message = "Pesel is required.")
    @Pattern(regexp = "^\\d{11}$", message = "Pesel must consist of 11 digits.")
    private String pesel;
    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;
    @Valid
    private PostAddressRequestDto address;
    @Basic
    @Nullable
    @Size(min = 2, message = "Description can't be shorter than 2 characters.")
    @Size(max = 1000, message = "Description can't be longer than 1000 characters.")
    private String description;
    @Nullable
    private String photo;
    @NotNull
    private boolean visible;
    @Nullable
    @ListSize(maxCount = 6)
    @TrainerTagSubset(anyOf = {
            TrainerTagType.BODYBUILDING,
            TrainerTagType.FUNCTIONAL_TRAINING,
            TrainerTagType.CROSS_TRAINING,
            TrainerTagType.WEIGHT_LOSS,
            TrainerTagType.MARTIAL_ARTS,
            TrainerTagType.BODYWEIGHT,
            TrainerTagType.WEIGHTLIFTING,
            TrainerTagType.MOTOR_PREPARATION,
            TrainerTagType.MEDICAL_TRAINING,
            TrainerTagType.PREGNANT_WOMEN,
            TrainerTagType.SENIOR_TRAINING,
            TrainerTagType.REDUCTION_TRAINING,
            TrainerTagType.PHYSIOTHERAPIST
    })
    private List<TrainerTagType> trainerTags;

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
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

    public PostAddressRequestDto getAddress() {
        return address;
    }

    public void setAddress(PostAddressRequestDto address) {
        this.address = address;
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

    @Nullable
    public List<TrainerTagType> getTrainerTags() {
        return trainerTags;
    }

    public void setTrainerTags(@Nullable List<TrainerTagType> trainerTags) {
        this.trainerTags = trainerTags;
    }
}
