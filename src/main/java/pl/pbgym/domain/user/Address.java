package pl.pbgym.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Table(name="address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq_gen")
    @SequenceGenerator(name="address_seq_gen", sequenceName="ADDRESS_SEQ", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "street_name", nullable = false)
    private String streetName;

    @Column(name = "building_number", nullable = false)
    private Integer buildingNumber;

    @Nullable
    @Column(name = "apartment_number")
    private Integer apartmentNumber;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private AbstractUser abstractUser;

    public Address() {
    }

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

    public Integer getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(Integer buildingNr) {
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

