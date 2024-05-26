package pl.pbgym.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name="abstract_user")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AbstractUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "abstract_user_seq_gen")
    @SequenceGenerator(name="abstract_user_seq_gen", sequenceName="ABSTRACT_USER_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false)
//    @Email
//    @NotEmpty(message = "Email jest wymagany.")
    private String email;

//    @Basic
//    @NotEmpty(message = "Hasło jest wymagane.")
//    @Size(min = 6, message = "Hasło musi być dłuższe niż 5 liter.")
    @Column(name = "password", nullable = false)
    private String password;

//    @Basic
//    @NotEmpty(message = "Imię jest wymagane.")
    @Column(name = "name", nullable = false)
//    @Size(min = 2, message = "Imię nie może być krótsze niż 2 litery.")
//    @Size(max = 20, message = "Imię nie może być dłuższe niż 20 liter.")
//    @Pattern(regexp = "[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ]*$", message = "Imię musi zaczynać się wielką literą i zawierać jedynie litery.")
    private String name;

//    @Basic
//    @NotEmpty(message = "Nazwisko jest wymagane.")
//    @Size(min = 2, message = "Imię nie może być krótsze niż 2 litery.")
//    @Size(max = 50, message = "Imię nie może być dłuższe niż 50 liter.")
//    @Pattern(regexp = "[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ]*$", message = "Nazwisko musi zaczynać się wielką literą i zawierać jedynie litery.")
    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "pesel", nullable = false)
    private String pesel;

//    @NotEmpty(message = "Numer jest wymagany")
//    @Pattern(regexp = "^[0-9]{9}$", message = "Numer telefonu powinien składać się z 9 cyfr.")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="address_id", referencedColumnName = "id", nullable = false)
    private Address address;

    public AbstractUser() {
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority;
        return switch (this) {
            case Member member -> Collections.singletonList(new SimpleGrantedAuthority("USER"));
            case Trainer trainer -> Collections.singletonList(new SimpleGrantedAuthority("TRAINER"));
            case Worker worker ->
                //TODO: Gdy worker bedzie mial liste permisji to trzeba ja tu zwrocic
                //return (Worker)this.getPermissions()
                    Collections.singletonList(new SimpleGrantedAuthority("WORKER"));
            default -> Collections.emptyList();
        };
    }

    public Long getId() {
        return id;
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
