package pl.pbgym.dto.user.trainer;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pl.pbgym.dto.user.UpdateAddressRequestDto;

public class UpdateTrainerRequestDto {

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;

    @Valid
    private UpdateAddressRequestDto address;

    @Basic
    @Column(name="description")
    private String description;

    @Column(name="photo")
    private byte[] photo;

    public UpdateTrainerRequestDto() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UpdateAddressRequestDto getAddress() {
        return address;
    }

    public void setAddress(UpdateAddressRequestDto address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
}
