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
            PermissionType.MEMBER_MANAGEMENT,
            PermissionType.TRAINER_MANAGEMENT,
            PermissionType.PASS_MANAGEMENT,
            PermissionType.GROUP_CLASS_MANAGEMENT,
            PermissionType.BLOG,
    }, message = "Permission need to be of ADMIN, STATISTICS, MEMBER_MANAGEMENT, TRAINER_MANAGEMENT," +
            " PASS_MANAGEMENT, GROUP_CLASS_MANAGEMENT, BLOG")
    @NotNull
    private List<PermissionType> permissions;

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
