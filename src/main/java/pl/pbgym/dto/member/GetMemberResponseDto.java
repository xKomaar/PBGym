package pl.pbgym.dto.member;

import pl.pbgym.domain.Address;

import java.time.LocalDate;

public class GetMemberResponseDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private LocalDate birthdate;
    private String pesel;
    private String phoneNumber;
    private Address address;

    public GetMemberResponseDto() {

    }

    public GetMemberResponseDto(Long id, String email, String name,
                                String surname, LocalDate birthdate, String pesel,
                                String phoneNumber, Address address) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.pesel = pesel;
        this.phoneNumber = phoneNumber;
        this.address = address;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
