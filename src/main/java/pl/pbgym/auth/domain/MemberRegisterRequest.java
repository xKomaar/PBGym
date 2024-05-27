package pl.pbgym.auth.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

public class MemberRegisterRequest {
    @Email
    @NotBlank(message = "Email is required.")
    private String email;

    @Size(min = 8, message = "Password can't be shorter than 8 characters long.")
    @Size(max = 50, message = "Password can't be longer than 50 characters long.")
    @NotBlank(message = "Password is required.")
    private String password;

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

    @NotBlank(message = "Pesel is required.")
    @Pattern(regexp = "^\\d{11}$", message = "Pesel must consist of 11 digits.")
    private String pesel;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;

    @Valid
    private AddressRequest address;

    public MemberRegisterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public AddressRequest getAddress() {
        return address;
    }

    public void setAddress(AddressRequest address) {
        this.address = address;
    }
}