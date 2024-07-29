package pl.pbgym.dto.user.worker;

import pl.pbgym.domain.user.Permissions;
import pl.pbgym.dto.user.GetAddressResponseDto;

import java.time.LocalDate;
import java.util.List;

public class GetWorkerResponseDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private LocalDate birthdate;
    private String pesel;
    private String phoneNumber;
    private GetAddressResponseDto address;
    private String idCardNumber;
    private String position;
    private List<Permissions> permissions;

    public GetWorkerResponseDto() {
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

    public GetAddressResponseDto getAddress() {
        return address;
    }

    public void setAddress(GetAddressResponseDto address) {
        this.address = address;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<Permissions> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permissions> permissions) {
        this.permissions = permissions;
    }
}
