package pl.pbgym.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
    private Integer id;

    @Column(name = "email", nullable = false)
    @Email
    @NotEmpty(message = "Email jest wymagany.")
    private String email;

    @Basic
    @NotEmpty(message = "Hasło jest wymagane.")
    @Size(min = 6, message = "Hasło musi być dłuższe niż 5 liter.")
    @Column(name = "password", nullable = false)
    private String password;

    @Basic
    @NotEmpty(message = "Imię jest wymagane.")
    @Column(name = "name", nullable = false)
    @Size(min = 2, message = "Imię nie może być krótsze niż 2 litery.")
    @Size(max = 20, message = "Imię nie może być dłuższe niż 20 liter.")
    @Pattern(regexp = "[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ]*$", message = "Imię musi zaczynać się wielką literą i zawierać jedynie litery.")
    private String name;

    @Basic
    @NotEmpty(message = "Nazwisko jest wymagane.")
    @Size(min = 2, message = "Imię nie może być krótsze niż 2 litery.")
    @Size(max = 50, message = "Imię nie może być dłuższe niż 50 liter.")
    @Pattern(regexp = "[A-Za-zżźćńółęąśŻŹĆĄŚĘŁÓŃ]*$", message = "Nazwisko musi zaczynać się wielką literą i zawierać jedynie litery.")
    @Column(name = "surname", nullable = false)
    private String surname;

    @NotEmpty(message = "Numer jest wymagany")
    @Pattern(regexp = "^[0-9]{9}$", message = "Numer telefonu powinien składać się z 9 cyfr.")
    @Column(name = "phone_nr", nullable = false)
    private String phoneNr;

    public AbstractUser() {
    }

    public AbstractUser(Integer id, String email, String password, String name, String surname, String phoneNr) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNr = phoneNr;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority;
        return switch (this) {
            case Member member -> Collections.singletonList(new SimpleGrantedAuthority("USER"));
            case Trainer trainer -> Collections.singletonList(new SimpleGrantedAuthority("DOCTOR"));
            case Worker worker ->
                //TODO: Gdy worker bedzie mial liste permisji to trzeba ja tu zwrocic
                //return (Worker)this.getPermissions()
                    Collections.singletonList(new SimpleGrantedAuthority("WORKER"));
            default -> Collections.emptyList();
        };
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getPhoneNr() {
        return phoneNr;
    }

    public void setPhoneNr(String phoneNr) {
        this.phoneNr = phoneNr;
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
