package pl.pbgym.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

public class UpdateAddressRequestDto {
    @Size(min = 2, message = "City name can't be shorter than 2 characters long.")
    @Size(max = 200, message = "City name can't be longer than 200 characters long.")
    @NotBlank(message = "City name is required.")
    private String city;

    @Size(min = 2, message = "Street Name can't be shorter than 2 characters long.")
    @Size(max = 200, message = "Street Name can't be longer than 200 characters long.")
    @NotBlank(message = "Street Name is required.")
    private String streetName;

    @NotNull
    @Digits(integer = 5, fraction = 0, message = "Building number must consist of only digits")
    private Integer buildingNumber;

    @Nullable
    @Digits(integer = 5, fraction = 0, message = "Building number must consist of only digits")
    private Integer apartmentNumber;

    @NotBlank(message = "Postal Code is required.")
    @Pattern(regexp = "^\\d{2}-\\d{3}$", message = "Postal code must be in the format dd-ddd")
    private String postalCode;

    public UpdateAddressRequestDto() {
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

    public void setBuildingNumber(Integer buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    @Nullable
    public Integer getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(@Nullable Integer apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
