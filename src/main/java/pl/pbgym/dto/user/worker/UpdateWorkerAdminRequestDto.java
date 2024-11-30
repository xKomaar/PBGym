package pl.pbgym.dto.user.worker;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.pbgym.domain.user.Gender;
import pl.pbgym.dto.auth.PostAddressRequestDto;
import pl.pbgym.validator.gender.GenderSubset;

import java.time.LocalDate;

public class UpdateWorkerAdminRequestDto {
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
    @Pattern(regexp = "^[A-Z]{3}\\d{6}$", message = "Wrong format of ID card number. Valid format example: XXX000000")
    @NotBlank(message = "ID card number is required.")
    private String idCardNumber;
    @Valid
    private PostAddressRequestDto address;

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

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public PostAddressRequestDto getAddress() {
        return address;
    }

    public void setAddress(PostAddressRequestDto address) {
        this.address = address;
    }
}
