package pl.pbgym.dto.user.worker;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import pl.pbgym.dto.user.UpdateAddressRequestDto;

public class UpdateWorkerRequestDto {
    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\d{9}$", message = "Phone number must consist of 9 digits.")
    private String phoneNumber;

    @Valid
    private UpdateAddressRequestDto address;

    public UpdateWorkerRequestDto() {
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
