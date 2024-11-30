package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.worker.Worker;
import pl.pbgym.validator.gender.GenderSubset;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name="abstract_user")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "abstract_user_seq_gen")
    @SequenceGenerator(name="abstract_user_seq_gen", sequenceName="ABSTRACT_USER_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "name", nullable = false)
    @Size(min = 2, message = "Name can't be shorter than 2 characters.")
    @Size(max = 50, message = "Name can't be longer than 50 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Name has to begin with a capital letter and involve only letters.")
    private String name;
    @Column(name = "surname", nullable = false)
    @Size(min = 2, message = "Surname can't be shorter than 2 characters.")
    @Size(max = 100, message = "Surname can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Surname has to begin with a capital letter and involve only letters.")
    private String surname;
    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;
    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    @GenderSubset(anyOf = {
            Gender.FEMALE,
            Gender.MALE,
            Gender.OTHER
    }, message = "Gender need to be MALE, FEMALE or OTHER")
    private Gender gender;
    @Column(name = "pesel", nullable = false)
    @Pattern(regexp = "^\\d{11}$", message = "Pesel must consist of 11 digits.")
    private String pesel;
    @Column(name = "phone_number", nullable = false)
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinColumn(name="address_id", referencedColumnName = "id", nullable = false)
    private Address address;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority;
        return switch (this) {
            case Member member -> Collections.singletonList(new SimpleGrantedAuthority("MEMBER"));
            case Trainer trainer -> Collections.singletonList(new SimpleGrantedAuthority("TRAINER"));
            case Worker worker -> Stream.concat(
                        Stream.of(new SimpleGrantedAuthority("WORKER")),
                        worker.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.get().name()))
                    ).collect(Collectors.toList());
            default -> Collections.emptyList();
        };
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
