package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq_gen")
    @SequenceGenerator(name="address_seq_gen", sequenceName="ADDRESS_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "city", nullable = false)
    @Size(min = 2, message = "City name can't be shorter than 2 characters long.")
    @Size(max = 200, message = "City name can't be longer than 200 characters long.")
    private String city;
    @Column(name = "street_name", nullable = false)
    @Size(min = 2, message = "Street Name can't be shorter than 2 characters long.")
    @Size(max = 200, message = "Street Name can't be longer than 200 characters long.")
    private String streetName;
    @Column(name = "building_number", nullable = false)
    @Pattern(regexp="(?i)^[1-9]\\d*(?: ?(?:[a-z]|[/-] ?\\d+[a-z]?))?$", message = "Building must be valid")
    private String buildingNumber;
    @Column(name = "apartment_number")
    @Nullable
    @Digits(integer = 5, fraction = 0, message = "Building number must consist of only digits")
    private Integer apartmentNumber;
    @Column(name = "postal_code", nullable = false)
    @Pattern(regexp = "^\\d{2}-\\d{3}$", message = "Postal code must be in the format dd-ddd")
    private String postalCode;
    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private AbstractUser abstractUser;

    public Long getId() {
        return id;
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

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNr) {
        this.buildingNumber = buildingNr;
    }

    @Nullable
    public Integer getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(@Nullable Integer apartmentNr) {
        this.apartmentNumber = apartmentNr;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public AbstractUser getAbstractUser() {
        return abstractUser;
    }

    public void setAbstractUser(AbstractUser abstractUser) {
        this.abstractUser = abstractUser;
    }
}

