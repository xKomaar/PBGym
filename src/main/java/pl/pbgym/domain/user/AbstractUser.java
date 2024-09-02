package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.pbgym.domain.user.member.Member;
import pl.pbgym.domain.user.trainer.Trainer;
import pl.pbgym.domain.user.worker.Worker;

import java.time.LocalDate;
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
    private String name;
    @Column(name = "surname", nullable = false)
    private String surname;
    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;
    @Column(name = "pesel", nullable = false)
    private String pesel;
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
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
