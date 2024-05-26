package pl.pbgym.domain;

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

    @Column(name = "building_nr", nullable = false)
    private Integer buildingNr;

    @Nullable
    @Column(name = "apartment_nr")
    private Integer apartmentNr;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @OneToOne(mappedBy = "address")
    @JsonIgnore
    private AbstractUser abstractUser;

    public Address(Long id, String city, String streetName, Integer buildingNr, @Nullable Integer apartmentNr, String postalCode, AbstractUser abstractUser) {
        this.id = id;
        this.city = city;
        this.streetName = streetName;
        this.buildingNr = buildingNr;
        this.apartmentNr = apartmentNr;
        this.postalCode = postalCode;
        this.abstractUser = abstractUser;
    }

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

    public Integer getBuildingNr() {
        return buildingNr;
    }

    public void setBuildingNr(Integer buildingNr) {
        this.buildingNr = buildingNr;
    }

    @Nullable
    public Integer getApartmentNr() {
        return apartmentNr;
    }

    public void setApartmentNr(@Nullable Integer apartmentNr) {
        this.apartmentNr = apartmentNr;
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

