package pl.pbgym.dto.worker;

import pl.pbgym.domain.Address;
import pl.pbgym.domain.Permissions;

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
    private Address address;
    private String idCardNumber;
    private String position;
    private List<Permissions> permissionList;

    public GetWorkerResponseDto() {
    }

    public GetWorkerResponseDto(Long id, String email, String name,
                                String surname, LocalDate birthdate, String pesel,
                                String phoneNumber, Address address, String idCardNumber,
                                String position, List<Permissions> permissionList) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.pesel = pesel;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.idCardNumber = idCardNumber;
        this.position = position;
        this.permissionList = permissionList;
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

    public List<Permissions> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<Permissions> permissionList) {
        this.permissionList = permissionList;
    }
}
