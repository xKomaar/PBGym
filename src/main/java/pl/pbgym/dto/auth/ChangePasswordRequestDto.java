package pl.pbgym.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequestDto {
    private String oldPassword;
    @Size(min = 8, message = "Password can't be shorter than 8 characters long.")
    @Size(max = 50, message = "Password can't be longer than 50 characters long.")
    @NotBlank(message = "Password is required.")
    private String newPassword;

    public ChangePasswordRequestDto() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
