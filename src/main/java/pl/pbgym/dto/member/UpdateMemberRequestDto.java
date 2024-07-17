package pl.pbgym.dto.member;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pl.pbgym.dto.UpdateAddressRequestDto;

public class UpdateMemberRequestDto {

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;
    @Valid
    private UpdateAddressRequestDto address;

    public UpdateMemberRequestDto() {
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
}
