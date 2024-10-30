package pl.pbgym.dto.user.worker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.pbgym.domain.user.worker.PermissionType;
import pl.pbgym.validator.permission.PermissionSubset;

import java.util.List;

public class UpdateWorkerAuthorityRequestDto {
    @Size(min = 2, message = "Position can't be shorter than 2 characters.")
    @Size(max = 100, message = "Position can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*(\\s[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*)*$", message = "Every word in position must begin with a capital letter.")
    @NotBlank(message = "Position is required.")
    private String position;

    @PermissionSubset(anyOf = {
            PermissionType.ADMIN,
            PermissionType.STATISTICS,
            PermissionType.USER_MANAGEMENT,
            PermissionType.NEWSLETTER,
            PermissionType.PASS_MANAGEMENT,
            PermissionType.GROUP_CLASSES_MANAGEMENT,
            PermissionType.BLOG,
    })
    @NotNull
    private List<PermissionType> permissions;

    public UpdateWorkerAuthorityRequestDto() {
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<PermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionType> permissions) {
        this.permissions = permissions;
    }
}
