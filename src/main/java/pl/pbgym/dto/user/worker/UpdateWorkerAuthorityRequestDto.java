package pl.pbgym.dto.user.worker;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.pbgym.domain.user.Permissions;
import pl.pbgym.validator.permission.PermissionSubset;

import java.util.List;

public class UpdateWorkerAuthorityRequestDto {
    @Size(min = 2, message = "Position can't be shorter than 2 characters.")
    @Size(max = 100, message = "Position can't be longer than 100 characters.")
    @Pattern(regexp = "^[A-ZŻŹĆĄŚĘŁÓŃ][a-zżźćńółęąś]*$", message = "Position has to begin with a capital letter and involve only letters.")
    @NotBlank(message = "Position is required.")
    private String position;

    @PermissionSubset(anyOf = {
            Permissions.ADMIN,
            Permissions.STATISTICS,
            Permissions.USER_MANAGEMENT,
            Permissions.NEWSLETTER,
            Permissions.PASS_MANAGEMENT,
            Permissions.GROUP_CLASSES_MANAGEMENT,
            Permissions.BLOG,
            Permissions.SHOP_MANAGEMENT
    })
    @NotNull
    private List<Permissions> permissions;

    public UpdateWorkerAuthorityRequestDto() {
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<Permissions> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permissions> permissions) {
        this.permissions = permissions;
    }
}
