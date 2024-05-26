package pl.pbgym.auth.domain;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public class MemberWithAddressRegisterRequest {
    private String email;
    private String password;
    private String name;
    private String surname;
    private LocalDate birthdate;
    private String pesel;
    private String phoneNumber;
    private String city;
    private String streetName;
    private Integer buildingNr;
    private Integer apartmentNr;
    private String postalCode;

    public MemberWithAddressRegisterRequest() {
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public Integer getBuildingNr() {
        return buildingNr;
    }

    public void setBuildingNr(Integer buildingNr) {
        this.buildingNr = buildingNr;
    }

    @Nullable
    public Integer getApartmentNr() {
        return apartmentNr;
    }

    public void setApartmentNr(@Nullable Integer apartmentNr) {
        this.apartmentNr = apartmentNr;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}