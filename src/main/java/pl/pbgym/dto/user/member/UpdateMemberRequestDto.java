package pl.pbgym.dto.user.member;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pl.pbgym.dto.auth.PostAddressRequestDto;

public class UpdateMemberRequestDto {

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;
    @Valid
    private PostAddressRequestDto address;

    public UpdateMemberRequestDto() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PostAddressRequestDto getAddress() {
        return address;
    }

    public void setAddress(PostAddressRequestDto address) {
        this.address = address;
    }
}
