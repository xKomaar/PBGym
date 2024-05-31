package pl.pbgym.auth.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WorkerRegisterRequest extends AbstractUserRequest {
    @Pattern(regexp = "^[A-Z]{3}\\d{6}$", message = "Wrong format of ID card number. Valid format example: XXX000000")
    @NotBlank(message = "ID card number is required.")
    private String IdCardNumber;
    @Size(min = 2, message = "Position can't be shorter than 2 characters.")
    @Size(max = 100, message = "Position can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Position has to begin with a capital letter and involve only letters.")
    @NotBlank(message = "Position is required.")
    private String position;

    public WorkerRegisterRequest() {
        super();
    }

    public String getIdCardNumber() {
        return IdCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        IdCardNumber = idCardNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
