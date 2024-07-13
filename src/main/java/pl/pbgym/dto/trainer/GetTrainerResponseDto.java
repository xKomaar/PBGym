package pl.pbgym.dto.trainer;

import pl.pbgym.domain.Address;

import java.time.LocalDate;

public class GetTrainerResponseDto {
    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String surname;
    private final LocalDate birthdate;
    private final String pesel;
    private final String phoneNumber;
    private final Address address;

    public GetTrainerResponseDto(Long id, String email, String password,
                                String name, String surname, LocalDate birthdate, String pesel,
                                String phoneNumber, Address address) {
        this.id = id;
        this.email = email;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public String getPesel() {
        return pesel;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Address getAddress() {
        return address;
    }
}
