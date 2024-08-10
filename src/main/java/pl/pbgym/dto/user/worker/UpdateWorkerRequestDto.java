package pl.pbgym.dto.user.worker;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pl.pbgym.dto.auth.PostAddressRequestDto;

public class UpdateWorkerRequestDto {
    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;

    @Valid
    private PostAddressRequestDto address;

    public UpdateWorkerRequestDto() {
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
