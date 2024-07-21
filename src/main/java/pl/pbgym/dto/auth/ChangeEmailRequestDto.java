package pl.pbgym.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ChangeEmailRequestDto {

    @Email
    @NotBlank
    private String newEmail;

    public ChangeEmailRequestDto() {
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}
